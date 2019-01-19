package info.smart_tools.smartactors.http_endpoint_plugins.http_endpoint_plugin;


import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.IAdditionDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.exception.AdditionDependencyStrategyException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecutionException;
import info.smart_tools.smartactors.base.iup_counter.IUpCounter;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.class_management.module_manager.ModuleManager;
import info.smart_tools.smartactors.endpoint.interfaces.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.endpoint.interfaces.imessage_mapper.IMessageMapper;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.http_endpoint.channel_handler_netty.ChannelHandlerNetty;
import info.smart_tools.smartactors.http_endpoint.cookies_setter.CookiesSetter;
import info.smart_tools.smartactors.http_endpoint.deserialize_strategy_get.DeserializeStrategyGet;
import info.smart_tools.smartactors.http_endpoint.deserialize_strategy_get.parse_tree.IParseTree;
import info.smart_tools.smartactors.http_endpoint.deserialize_strategy_get.parse_tree.ParseTree;
import info.smart_tools.smartactors.http_endpoint.deserialize_strategy_post_form_urlencoded.DeserializeStrategyPostFormUrlencoded;
import info.smart_tools.smartactors.http_endpoint.deserialize_strategy_post_json.DeserializeStrategyPostJson;
import info.smart_tools.smartactors.http_endpoint.environment_handler.EnvironmentHandler;
import info.smart_tools.smartactors.http_endpoint.http_endpoint.HttpEndpoint;
import info.smart_tools.smartactors.http_endpoint.http_headers_setter.HttpHeadersExtractor;
import info.smart_tools.smartactors.http_endpoint.http_response_sender.HttpResponseSender;
import info.smart_tools.smartactors.http_endpoint.message_to_bytes_mapper.MessageToBytesMapper;
import info.smart_tools.smartactors.http_endpoint.respons_status_extractor.ResponseStatusExtractor;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.field_name_tools.FieldNames;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.util.List;

/**
 * Plugin, that register {@link HttpEndpoint} and {@link HttpResponseSender} at {@link IOC}
 */
public class HttpEndpointPlugin implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    private IFieldName portFieldName;
    private IFieldName startChainNameFieldName;
    private IFieldName stackDepthFieldName;
    private IFieldName maxContentLengthFieldName;
    private IFieldName endpointNameFieldName;
    private IFieldName queueFieldName;
    private IFieldName templatesFieldName;
    private IFieldName scopeSwitchingFieldName;

    /**
     * Constructor
     *
     * @param bootstrap bootstrap
     */
    public HttpEndpointPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    // ToDo: replace duplicate code
    @SuppressWarnings({"unchecked", "Duplicates"})
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("CreateHttpEndpoint");
            item
                    .after("EndpointPlugin")
                    .after("response")
                    .after("response_content_strategy")
                    .before("configure")
                    .process(() -> {
                        try {
                            initializeFieldNames();

                            IKey httpEndpointKey = Keys.resolveByName("http_endpoint");

                            registerCookiesSetter();
                            registerHeadersExtractor();
                            registerResponseStatusExtractor();
                            registerExceptionalResponse();

                            IOC.register(
                                    Keys.resolveByName(IEnvironmentHandler.class.getCanonicalName()),
                                    new ApplyFunctionToArgumentsStrategy(
                                            (args) -> {
                                                IObject configuration = (IObject) args[0];
                                                IQueue queue;
                                                Integer stackDepth;
                                                Boolean scopeSwitching;
                                                try {
                                                    queue = (IQueue) configuration.getValue(queueFieldName);
                                                    stackDepth =
                                                            (Integer) configuration.getValue(stackDepthFieldName);
                                                    scopeSwitching = (Boolean) configuration.getValue(scopeSwitchingFieldName);
                                                    if (scopeSwitching == null) {
                                                        scopeSwitching = true;
                                                    }
                                                    return new EnvironmentHandler(queue, stackDepth, scopeSwitching);
                                                } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                    )
                            );

                            IOC.register(httpEndpointKey,
                                    new ApplyFunctionToArgumentsStrategy(
                                            (args) -> {
                                                IObject configuration = (IObject) args[0];

                                                try {
                                                    String endpointName = (String) configuration.getValue(endpointNameFieldName);

                                                    IOC.resolve(
                                                            Keys.resolveByName("info.smart_tools.smartactors.endpoint.interfaces.ideserialize_strategy.IDeserializeStrategy"),
                                                            "HTTP_GET",
                                                            endpointName,
                                                            configuration.getValue(templatesFieldName)
                                                    );
                                                    IOC.resolve(
                                                            Keys.resolveByName("info.smart_tools.smartactors.endpoint.interfaces.ideserialize_strategy.IDeserializeStrategy"),
                                                            "HTTP_application/json",
                                                            endpointName
                                                    );
                                                    IOC.resolve(
                                                            Keys.resolveByName("info.smart_tools.smartactors.endpoint.interfaces.ideserialize_strategy.IDeserializeStrategy"),
                                                            "HTTP_application/x-www-form-urlencoded",
                                                            endpointName
                                                    );
                                                    IOC.register(
                                                            Keys.resolveByName(endpointName + "_endpoint-config"),
                                                            new SingletonStrategy(configuration)
                                                    );

                                                    IUpCounter upCounter = IOC.resolve(Keys.resolveByName("root upcounter"));

                                                    IEnvironmentHandler environmentHandler = IOC.resolve(
                                                            Keys.resolveByName(IEnvironmentHandler.class.getCanonicalName()),
                                                            configuration);
                                                    HttpEndpoint endpoint = new HttpEndpoint(
                                                            (Integer) configuration.getValue(portFieldName),
                                                            (Integer) configuration.getValue(maxContentLengthFieldName),
                                                            ScopeProvider.getCurrentScope(),
                                                            ModuleManager.getCurrentModule(),
                                                            environmentHandler,
                                                            configuration.getValue(startChainNameFieldName),
                                                            (String) configuration.getValue(endpointNameFieldName),
                                                            upCounter);

                                                    upCounter.onShutdownComplete(this.toString(), endpoint::stop);

                                                    return endpoint;
                                                } catch (Exception e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                    )
                            );

                            registerDeserializationStrategies();
                            registerResponseSenders();

                            IKey emptyIObjectKey = Keys.resolveByName("EmptyIObject");
                            IOC.register(
                                    emptyIObjectKey,
                                    new ApplyFunctionToArgumentsStrategy((args) -> new DSObject())
                            );

                            IKey channelHandlerNettyKey = Keys.resolveByName(
                                    "info.smart_tools.smartactors.http_endpoint.channel_handler_netty.ChannelHandlerNetty"
                            );
                            IOC.register(channelHandlerNettyKey,
                                    new ApplyFunctionToArgumentsStrategy(
                                            (args) -> {
                                                ChannelHandlerNetty channelHandlerNetty = new ChannelHandlerNetty();
                                                channelHandlerNetty.init((ChannelHandlerContext) args[0]);
                                                return channelHandlerNetty;
                                            }
                                    ));

                        } catch (ResolutionException e) {
                            throw new ActionExecutionException("EndpointCollection plugin can't load: can't get key", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecutionException("EndpointCollection plugin can't load: can't create strategy", e);
                        } catch (RegistrationException | AdditionDependencyStrategyException e) {
                            throw new ActionExecutionException("EndpointCollection plugin can't load: can't register new strategy", e);
                        }
                    })
                    .revertProcess(() -> {
                        String itemName = "CreateHttpEndpoint";
                        String keyName = "";

                        try {
                            keyName = "info.smart_tools.smartactors.http_endpoint.channel_handler_netty.ChannelHandlerNetty";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = "EmptyIObject";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = "http_request_key_for_response_sender";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = IParseTree.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = "http_request_key_for_deserialize";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = "http_endpoint";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = IEnvironmentHandler.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = "HttpShuttingDownException";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = "HttpInternalException";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = "HttpRequestParametersToIObjectException";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = "HttpPostParametersToIObjectException";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = "key_for_response_status_setter";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = "key_for_headers_extractor";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = "key_for_cookies_extractor";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }
                    });

            bootstrap.add(item);
        } catch (Exception e) {
            throw new PluginException("Can't load \"CreateHttpEndpoint\" plugin", e);
        }
    }

    private void initializeFieldNames() throws ResolutionException {
        portFieldName =
                IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyByNameResolutionStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                        "port"
                );
        startChainNameFieldName =
                IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyByNameResolutionStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                        "startChain"
                );
        stackDepthFieldName =
                IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyByNameResolutionStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                        "stackDepth"
                );
        maxContentLengthFieldName =
                IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyByNameResolutionStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                        "maxContentLength"
                );
        endpointNameFieldName =
                IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyByNameResolutionStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                        "name"
                );

        queueFieldName =
                IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyByNameResolutionStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                        "queue"
                );

        templatesFieldName =
                IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyByNameResolutionStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                        "templates"
                );
        scopeSwitchingFieldName = FieldNames.resolveByName("scopeSwitching");
    }

    private void registerResponseSenders() throws ResolutionException, InvalidArgumentException, RegistrationException,
            AdditionDependencyStrategyException {
        IAdditionDependencyStrategy responseSenderChooser =
                IOC.resolve(Keys.resolveByName("ResponseSenderChooser"));

        IOC.register(Keys.resolveByName("http_request_key_for_response_sender"), new ApplyFunctionToArgumentsStrategy(
                        (args) ->
                                "HTTP_POST"

                )
        );

        responseSenderChooser.register("HTTP_POST",
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            //args[0] - type of the request
                            //args[1] - name of the endpoint
                            try {
                                return new HttpResponseSender((String) args[1]);
                            } catch (ResolutionException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
        );
    }

    private void registerCookiesSetter() throws ResolutionException, InvalidArgumentException,
            RegistrationException, AdditionDependencyStrategyException {

        IAdditionDependencyStrategy cookiesSetterChooser =
                IOC.resolve(Keys.resolveByName("CookiesSetterChooser"));

        IOC.register(
                Keys.resolveByName("key_for_cookies_extractor"),
                new ApplyFunctionToArgumentsStrategy((args) -> "HTTP")
        );

        cookiesSetterChooser.register("HTTP", new SingletonStrategy(new CookiesSetter()));
    }

    private void registerHeadersExtractor() throws ResolutionException, InvalidArgumentException, RegistrationException,
            AdditionDependencyStrategyException {
        IAdditionDependencyStrategy cookiesSetterChooser =
                IOC.resolve(Keys.resolveByName("HeadersExtractorChooser"));

        IOC.register(Keys.resolveByName("key_for_headers_extractor"), new ApplyFunctionToArgumentsStrategy(
                        (args) ->
                                "HTTP"
                )
        );

        cookiesSetterChooser.register("HTTP", new SingletonStrategy(new HttpHeadersExtractor()));
    }


    private void registerResponseStatusExtractor() throws ResolutionException, InvalidArgumentException,
            RegistrationException, AdditionDependencyStrategyException {

        IAdditionDependencyStrategy cookiesSetterChooser =
                IOC.resolve(Keys.resolveByName("ResponseStatusSetter"));

        IOC.register(
                Keys.resolveByName("key_for_response_status_setter"),
                new ApplyFunctionToArgumentsStrategy((args) -> "HTTP")
        );

        cookiesSetterChooser.register("HTTP",
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            try {
                                return new ResponseStatusExtractor();
                            } catch (ResolutionException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
        );
    }

    @SuppressWarnings("unchecked")
    private void registerDeserializationStrategies() throws ResolutionException, InvalidArgumentException,
            RegistrationException, AdditionDependencyStrategyException {

        IAdditionDependencyStrategy deserializationStrategyChooser =
                IOC.resolve(Keys.resolveByName("DeserializationStrategyChooser"));

        IMessageMapper messageMapper = new MessageToBytesMapper();

        IOC.register(Keys.resolveByName("http_request_key_for_deserialize"), new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            FullHttpRequest request = (FullHttpRequest) args[0];
                            if (request.method().toString().equals("GET")) {
                                return "HTTP_GET";
                            }
                            return "HTTP_" + request.headers().get(HttpHeaderNames.CONTENT_TYPE);
                        }
                )
        );

        deserializationStrategyChooser.register("HTTP_application/json",
                new ApplyFunctionToArgumentsStrategy(
                        //args[0] - type of the request
                        //args[1] - name of the endpoint
                        (args) -> new DeserializeStrategyPostJson(messageMapper)
                )
        );
        deserializationStrategyChooser.register("HTTP_application/x-www-form-urlencoded",
                new ApplyFunctionToArgumentsStrategy(
                        //args[0] - type of the request
                        //args[1] - name of the endpoint
                        (args) -> new DeserializeStrategyPostFormUrlencoded()
                )
        );
        deserializationStrategyChooser.register("HTTP_GET",
                new ApplyFunctionToArgumentsStrategy(
                        //args[0] - type of the request
                        //args[1] - name of the endpoint
                        (args) -> {
                            try {
                                return new DeserializeStrategyGet((List<String>) args[2]);
                            } catch (ResolutionException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
        );
        IParseTree tree = new ParseTree();
        IOC.register(Keys.resolveByName(IParseTree.class.getCanonicalName()), new SingletonStrategy(
                        tree
                )
        );
    }

    private void registerExceptionalResponse() throws InvalidArgumentException, ResolutionException, RegistrationException {
        IOC.register(Keys.resolveByName("HttpPostParametersToIObjectException"), new SingletonStrategy(
                        new DSObject("{\"exception\": \"Request body is not json\", \"statusCode\": 400}")
                )
        );
        IOC.register(Keys.resolveByName("HttpRequestParametersToIObjectException"), new SingletonStrategy(
                        new DSObject("{\"exception\": \"This url is not registered\", \"statusCode\": 404}")
                )
        );
        IOC.register(Keys.resolveByName("HttpInternalException"), new SingletonStrategy(
                        new DSObject("{\"exception\": \"Internal server error\", \"statusCode\": 500}")
                )
        );
        IOC.register(Keys.resolveByName("HttpShuttingDownException"), new SingletonStrategy(
                        new DSObject("{\"exception\": \"Service not available (shutting down)\", \"statusCode\": 503}")
                )
        );
    }
}
