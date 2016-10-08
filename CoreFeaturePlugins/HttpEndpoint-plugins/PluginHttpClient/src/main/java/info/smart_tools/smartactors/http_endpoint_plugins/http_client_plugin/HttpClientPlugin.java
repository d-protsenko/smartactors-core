package info.smart_tools.smartactors.http_endpoint_plugins.http_client_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.endpoint.interfaces.ideserialize_strategy.IDeserializeStrategy;
import info.smart_tools.smartactors.endpoint.interfaces.imessage_mapper.IMessageMapper;
import info.smart_tools.smartactors.endpoint.interfaces.irequest_sender.exception.RequestSenderException;
import info.smart_tools.smartactors.endpoint.interfaces.iresponse_handler.IResponseHandler;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.http_endpoint.http_client.HttpClient;
import info.smart_tools.smartactors.http_endpoint.http_client_initializer.HttpClientInitializer;
import info.smart_tools.smartactors.http_endpoint.http_response_deserialization_strategy.HttpResponseDeserializationStrategy;
import info.smart_tools.smartactors.http_endpoint.http_response_handler.HttpResponseHandler;
import info.smart_tools.smartactors.http_endpoint.message_to_bytes_mapper.MessageToBytesMapper;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Plugin for http client
 */
public class HttpClientPlugin implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    public HttpClientPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    IFieldName uriFieldName;
    private IFieldName messageFieldName, startChainNameFieldName, queueFieldName, stackDepthFieldName, uuidFieldName, messageMapIdFieldName;

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("CreateHttpClient");
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
                                    registerFieldNames();
                                    IOC.register(Keys.getOrAdd(URI.class.getCanonicalName()), new CreateNewInstanceStrategy(
                                                    (args) -> {
                                                        try {
                                                            return new URI((String) args[0]);
                                                        } catch (URISyntaxException e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                    }
                                            )
                                    );
                                    IMessageMapper<byte[]> messageMapper = new MessageToBytesMapper();

                                    IDeserializeStrategy deserializeStrategy = new HttpResponseDeserializationStrategy(messageMapper);

                                    IOC.register(Keys.getOrAdd("httpResponseResolver"), new SingletonStrategy(
                                                    deserializeStrategy
                                            )
                                    );

                                    IOC.register(Keys.getOrAdd("EmptyIObject"), new CreateNewInstanceStrategy(
                                                    (args) -> new DSObject()
                                            )
                                    );
                                    IOC.register(Keys.getOrAdd(IResponseHandler.class.getCanonicalName()), new CreateNewInstanceStrategy(
                                                    (args) -> {
                                                        try {
                                                            IObject configuration = IOC.resolve(Keys.getOrAdd("responseHandlerConfiguration"));
                                                            String uuid = (String) args[0];
                                                            String messageMapId = (String) args[1];
                                                            IResponseHandler responseHandler = new HttpResponseHandler(
                                                                    (IQueue<ITask>) configuration.getValue(queueFieldName),
                                                                    (Integer) configuration.getValue(stackDepthFieldName),
                                                                    (IReceiverChain) configuration.getValue(startChainNameFieldName),
                                                                    messageMapId
                                                            );
                                                            return responseHandler;
                                                        } catch (ResolutionException | ReadValueException | InvalidArgumentException e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                    }
                                            )
                                    );

                                    IOC.register(Keys.getOrAdd("sendHttpRequest"), new ApplyFunctionToArgumentsStrategy(
                                                    (args) -> {
                                                        try {
                                                            HttpClient client = (HttpClient) args[0];
                                                            IObject request = (IObject) args[1];
                                                            client.sendRequest(request);
                                                            IOC.resolve(
                                                                    Keys.getOrAdd("createTimerOnRequest"),
                                                                    request.getValue(messageFieldName),
                                                                    request.getValue(messageMapIdFieldName)
                                                            );
                                                            return client;
                                                        } catch (ResolutionException | RequestSenderException | ReadValueException e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                    }
                                            )
                                    );

                                    IOC.register(Keys.getOrAdd("getHttpClient"), new ApplyFunctionToArgumentsStrategy(
                                                    (args) -> {
                                                        IObject request = (IObject) args[0];
                                                        try {
                                                            IResponseHandler responseHandler = IOC.resolve(
                                                                    Keys.getOrAdd(IResponseHandler.class.getCanonicalName()),
                                                                    request.getValue(uuidFieldName),
                                                                    request.getValue(messageMapIdFieldName)
                                                            );
                                                            HttpClient client =
                                                                    new HttpClient(
                                                                            URI.create((String) request.getValue(uriFieldName)),
                                                                            responseHandler
                                                                    );
                                                            return client;
                                                        } catch (ReadValueException | ResolutionException | RequestSenderException e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                    }
                                            )
                                    );
                                    HttpClientInitializer.init();
                                } catch (RegistrationException | ResolutionException | InvalidArgumentException e) {
                                    e.printStackTrace();
                                    throw new RuntimeException(e);
                                }
                            }
                    );
            bootstrap.add(item);

        } catch (Exception e) {
            throw new PluginException(e);
        }
    }

    private void registerFieldNames() throws ResolutionException {
        uriFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "uri");
        this.startChainNameFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "startChain");
        this.queueFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "queue");
        this.stackDepthFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "stackDepth");
        uuidFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "stackDepth");
        messageMapIdFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "messageMapId");
        messageFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "message");
    }
}
