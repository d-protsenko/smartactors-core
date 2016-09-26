package info.smart_tools.smartactors.plugin.http_client_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.IDeserializeStrategy;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.http_client.HttpClient;
import info.smart_tools.smartactors.core.http_client_handler.HttpClientHandler;
import info.smart_tools.smartactors.core.http_response_deserialization_strategy.HttpResponseDeserializationStrategy;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.imessage_mapper.IMessageMapper;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.irequest_sender.exception.RequestSenderException;
import info.smart_tools.smartactors.core.message_to_bytes_mapper.MessageToBytesMapper;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by sevenbits on 26.09.16.
 */
public class HttpClientPlugin implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    public HttpClientPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    IFieldName uriFieldName;

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

                                    ChannelInboundHandler channelInboundHandler =
                                            new SimpleChannelInboundHandler<FullHttpResponse>(FullHttpResponse.class) {
                                                @Override
                                                protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {

                                                }
                                            };
                                    HttpClientHandler handler = new HttpClientHandler(null);
                                    IOC.register(Keys.getOrAdd("httpClient"), new CreateNewInstanceStrategy(
                                                    (args) -> {
                                                        IObject requestConfiguration = (IObject) args[0];
                                                        try {
                                                            HttpClient client = new HttpClient(
                                                                    IOC.resolve(
                                                                            Keys.getOrAdd(URI.class.getCanonicalName()),
                                                                            requestConfiguration.getValue(uriFieldName)
                                                                    ),
                                                                    handler

                                                            );
                                                            client.start();
                                                            return client;
                                                        } catch (ResolutionException | InvalidArgumentException
                                                                | ReadValueException | RequestSenderException e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                    }
                                            )
                                    );
                                    IOC.register(Keys.getOrAdd("stopHttpClient"), new ApplyFunctionToArgumentsStrategy(
                                                    (args) -> {
                                                        HttpClient client = (HttpClient) args[0];
                                                        return client.stop();
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
                                } catch (RegistrationException e) {
                                    e.printStackTrace();
                                } catch (ResolutionException e) {
                                    e.printStackTrace();
                                } catch (InvalidArgumentException e) {
                                    e.printStackTrace();
                                }
                            }
                    );
            bootstrap.add(item);

        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    private void registerFieldNames() throws ResolutionException {
        uriFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "uri");
    }
}
