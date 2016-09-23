package info.smart_tools.smartactors.plugin.https_endpoint;

import info.smart_tools.smartactors.core.HttpEndpoint;
import info.smart_tools.smartactors.core.IDeserializeStrategy;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.channel_handler_netty.ChannelHandlerNetty;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.deserialize_strategy_get.DeserializeStrategyGet;
import info.smart_tools.smartactors.core.deserialize_strategy_post_json.DeserializeStrategyPostJson;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.environment_handler.EnvironmentHandler;
import info.smart_tools.smartactors.core.http_response_sender.HttpResponseSender;
import info.smart_tools.smartactors.core.https_endpoint.HttpsEndpoint;
import info.smart_tools.smartactors.core.i_addition_dependency_strategy.IAdditionDependencyStrategy;
import info.smart_tools.smartactors.core.i_addition_dependency_strategy.exception.AdditionDependencyStrategyException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.imessage_mapper.IMessageMapper;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.issl_engine_provider.ISslEngineProvider;
import info.smart_tools.smartactors.core.issl_engine_provider.exception.SSLEngineProviderException;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.message_to_bytes_mapper.MessageToBytesMapper;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.ssl_engine_provider.SslEngineProvider;
import info.smart_tools.smartactors.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.strategy.cookies_setter.CookiesSetter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Plugin for register http/https endpoint at IOC
 */
public class HttpsEndpointPlugin implements IPlugin {

    private IFieldName typeFieldName;
    private IFieldName portFieldName;
    private IFieldName startChainNameFieldName;
    private IFieldName stackDepthFieldName;
    private IFieldName maxContentLengthFieldName;
    private IFieldName endpointNameFieldName;
    private IFieldName queueFieldName;
    private IFieldName templatesFieldName;

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     *
     * @param bootstrap bootstrap
     */
    public HttpsEndpointPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("CreateHttpsEndpoint");
            item
                    .after("IOC")
                    .after("message_processor")
                    .after("message_processing_sequence")
                    .after("response")
                    .after("response_content_strategy")
                    .after("FieldNamePlugin")
                    .before("starter")
                    .process(
                            () -> {
                                try {
                                    initializeFieldNames();
                                    IOC.register(Keys.getOrAdd(ISslEngineProvider.class.getCanonicalName()),
                                            new CreateNewInstanceStrategy(
                                                    (args) -> {
                                                        ISslEngineProvider sslContextProvider = new SslEngineProvider();
                                                        try {
                                                            sslContextProvider.init((IObject) args[0]);
                                                        } catch (SSLEngineProviderException e) {
                                                        }
                                                        return sslContextProvider;
                                                    }
                                            )
                                    );

                                    IOC.register(
                                            Keys.getOrAdd(IEnvironmentHandler.class.getCanonicalName()),
                                            new CreateNewInstanceStrategy(
                                                    (args) -> {
                                                        IObject configuration = (IObject) args[0];
                                                        IQueue queue = null;
                                                        Integer stackDepth = null;
                                                        try {
                                                            queue = (IQueue) configuration.getValue(queueFieldName);
                                                            stackDepth =
                                                                    (Integer) configuration.getValue(stackDepthFieldName);
                                                            return new EnvironmentHandler(queue, stackDepth);
                                                        } catch (ReadValueException | InvalidArgumentException e) {
                                                        }
                                                        return null;
                                                    }
                                            )
                                    );
                                    registerHttpEndpoint();
                                    registerHttpsEndpoint();

                                    IOC.register(Keys.getOrAdd(FileInputStream.class.getCanonicalName()),
                                            new CreateNewInstanceStrategy(
                                                    (args) -> {
                                                        try {
                                                            return new FileInputStream((String) args[0]);
                                                        } catch (FileNotFoundException e) {
                                                        }
                                                        return null;
                                                    }
                                            ));
                                    registerDeserializationStrategies();
                                    registerResponseSenders();
                                    registerExceptionalResponse();
                                    registerCookiesSetter();
                                    IKey emptyIObjectKey = Keys.getOrAdd("EmptyIObject");
                                    IOC.register(emptyIObjectKey, new CreateNewInstanceStrategy(
                                                    (args) -> new DSObject()
                                            )
                                    );

                                    IKey channelHandlerNettyKey = Keys.getOrAdd(ChannelHandlerNetty.class.getCanonicalName());
                                    IOC.register(channelHandlerNettyKey,
                                            new CreateNewInstanceStrategy(
                                                    (args) -> {
                                                        ChannelHandlerNetty channelHandlerNetty = new ChannelHandlerNetty();
                                                        channelHandlerNetty.init((ChannelHandlerContext) args[0]);
                                                        return channelHandlerNetty;
                                                    }
                                            ));

                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }

                            }
                    );
            bootstrap.add(item);
        } catch (Exception e) {
            throw new PluginException("Can't load \"CreateHttpsEndpoint\" plugin", e);
        }
    }

    private void initializeFieldNames() throws ResolutionException {

        typeFieldName =
                IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                        "type"
                );
        portFieldName =
                IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                        "port"
                );
        startChainNameFieldName =
                IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                        "startChain"
                );
        stackDepthFieldName =
                IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                        "stackDepth"
                );
        maxContentLengthFieldName =
                IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                        "maxContentLength"
                );
        endpointNameFieldName =
                IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                        "endpointName"
                );

        queueFieldName =
                IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                        "queue"
                );

        templatesFieldName =
                IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                        "templates"
                );
    }


    private void registerHttpEndpoint() throws ResolutionException, InvalidArgumentException, RegistrationException {
        IOC.register(Keys.getOrAdd("http_endpoint"),
                new CreateNewInstanceStrategy(
                        (args) -> {
                            IObject configuration = (IObject) args[0];
                            try {
                                IOC.resolve(
                                        Keys.getOrAdd(IDeserializeStrategy.class.getCanonicalName()),
                                        "HTTP_GET",
                                        configuration.getValue(endpointNameFieldName),
                                        configuration.getValue(templatesFieldName));
                                IOC.resolve(
                                        Keys.getOrAdd(IDeserializeStrategy.class.getCanonicalName()),
                                        "HTTP_POST",
                                        configuration.getValue(endpointNameFieldName));


                                IEnvironmentHandler environmentHandler = IOC.resolve(
                                        Keys.getOrAdd(IEnvironmentHandler.class.getCanonicalName()),
                                        configuration);
                                return new HttpEndpoint((Integer) configuration.getValue(portFieldName),
                                        (Integer) configuration.getValue(maxContentLengthFieldName),
                                        ScopeProvider.getCurrentScope(), environmentHandler,
                                        (IReceiverChain) configuration.getValue(startChainNameFieldName),
                                        (String) configuration.getValue(endpointNameFieldName));
                            } catch (ReadValueException | InvalidArgumentException
                                    | ScopeProviderException | ResolutionException e) {
                            }
                            return null;
                        }
                )
        );
    }

    private void registerHttpsEndpoint() throws InvalidArgumentException, RegistrationException, ResolutionException {
        IKey httpsEndpointKey = Keys.getOrAdd("https_endpoint");
        IOC.register(httpsEndpointKey,
                new CreateNewInstanceStrategy(
                        (args) -> {
                            IObject configuration = (IObject) args[0];
                            try {
                                IOC.resolve(
                                        Keys.getOrAdd(IDeserializeStrategy.class.getCanonicalName()),
                                        "HTTP_GET",
                                        configuration.getValue(endpointNameFieldName),
                                        configuration.getValue(templatesFieldName));
                                IOC.resolve(
                                        Keys.getOrAdd(IDeserializeStrategy.class.getCanonicalName()),
                                        "HTTP_POST",
                                        configuration.getValue(endpointNameFieldName));

                                IEnvironmentHandler environmentHandler = IOC.resolve(
                                        Keys.getOrAdd(IEnvironmentHandler.class.getCanonicalName()),
                                        configuration);
                                ISslEngineProvider sslContextProvider =
                                        IOC.resolve(
                                                Keys.getOrAdd(ISslEngineProvider.class.getCanonicalName()),
                                                configuration
                                        );
                                return new HttpsEndpoint((Integer) configuration.getValue(portFieldName),
                                        (Integer) configuration.getValue(maxContentLengthFieldName),
                                        ScopeProvider.getCurrentScope(), environmentHandler,
                                        (String) configuration.getValue(endpointNameFieldName),
                                        (IReceiverChain) configuration.getValue(startChainNameFieldName),
                                        sslContextProvider);
                            } catch (ReadValueException | InvalidArgumentException
                                    | ScopeProviderException | ResolutionException e) {
                            }
                            return null;
                        }
                )
        );
    }

    private void registerCookiesSetter() throws ResolutionException, InvalidArgumentException, RegistrationException,
            AdditionDependencyStrategyException {
        IAdditionDependencyStrategy cookiesSetterChooser =
                IOC.resolve(Keys.getOrAdd("CookiesSetterChooser"));

        IOC.register(Keys.getOrAdd("key_for_cookies_extractor"), new ApplyFunctionToArgumentsStrategy(
                        (args) ->
                                "HTTP"
                )
        );

        cookiesSetterChooser.register("HTTP",
                new CreateNewInstanceStrategy(
                        (args) -> new CookiesSetter()
                )
        );
    }

    private void registerResponseSenders() throws ResolutionException, InvalidArgumentException, RegistrationException,
            AdditionDependencyStrategyException {
        IAdditionDependencyStrategy responseSenderChooser =
                IOC.resolve(Keys.getOrAdd("ResponseSenderChooser"));

        IOC.register(Keys.getOrAdd("http_request_key_for_response_sender"), new ApplyFunctionToArgumentsStrategy(
                        (args) ->
                                "HTTP_POST"

                )
        );

        responseSenderChooser.register("HTTP_POST",
                new CreateNewInstanceStrategy(
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

    private void registerDeserializationStrategies() throws ResolutionException, InvalidArgumentException, RegistrationException,
            AdditionDependencyStrategyException {
        IAdditionDependencyStrategy deserializationStrategyChooser =
                IOC.resolve(Keys.getOrAdd("DeserializationStrategyChooser"));
        IMessageMapper messageMapper = new MessageToBytesMapper();

        IOC.register(Keys.getOrAdd("http_request_key_for_deserialize"), new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            FullHttpRequest httpRequest = (FullHttpRequest) args[0];
                            return "HTTP_" + httpRequest.method().toString();
                        }
                )
        );

        deserializationStrategyChooser.register("HTTP_GET",
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new DeserializeStrategyGet((List<String>) args[0]);
                            } catch (ResolutionException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
        );
        deserializationStrategyChooser.register("HTTP_POST",
                new CreateNewInstanceStrategy(
                        (args) -> new DeserializeStrategyPostJson(messageMapper)
                )
        );

        ResolveByNameIocStrategy resolveStrategy = new ResolveByNameIocStrategy();
        IKey deserializeStrategyKey = Keys.getOrAdd(IDeserializeStrategy.class.getCanonicalName());
        IOC.register(deserializeStrategyKey, resolveStrategy);
    }

    private void registerExceptionalResponse() throws InvalidArgumentException, ResolutionException, RegistrationException {
        IOC.register(Keys.getOrAdd("HttpPostParametersToIObjectException"), new SingletonStrategy(
                        new DSObject("{\"exception\": \"Request body is not json\", \"statusCode\": 400}")
                )
        );
        IOC.register(Keys.getOrAdd("HttpRequestParametersToIObjectException"), new SingletonStrategy(
                        new DSObject("{\"exception\": \"This url is not registered\", \"statusCode\": 404}")
                )
        );
        IOC.register(Keys.getOrAdd("HttpInternalException"), new SingletonStrategy(
                        new DSObject("{\"exception\": \"Internal server error\", \"statusCode\": 500}")
                )
        );
    }
}
