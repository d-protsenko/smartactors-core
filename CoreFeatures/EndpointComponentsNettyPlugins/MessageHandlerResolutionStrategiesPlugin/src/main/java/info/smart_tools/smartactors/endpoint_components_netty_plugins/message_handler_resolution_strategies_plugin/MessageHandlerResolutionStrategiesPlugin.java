package info.smart_tools.smartactors.endpoint_components_netty_plugins.message_handler_resolution_strategies_plugin;

import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.IAdditionDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iaction.IFunction;
import info.smart_tools.smartactors.base.interfaces.iaction.IFunction0;
import info.smart_tools.smartactors.base.simple_strict_storage_strategy.SimpleStrictStorageStrategy;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.base.strategy.strategy_storage_strategy.StrategyStorageStrategy;
import info.smart_tools.smartactors.endpoint_component_netty.cookies_setter.CookiesSetter;
import info.smart_tools.smartactors.endpoint_component_netty.http_path_parse.HttpPathParse;
import info.smart_tools.smartactors.endpoint_components_generic.default_outbound_connection_channel.DefaultOutboundConnectionChannel;
import info.smart_tools.smartactors.endpoint_components_generic.message_handler_resolution_strategy.MessageHandlerResolutionStrategy;
import info.smart_tools.smartactors.endpoint_components_generic.parse_tree.IParseTree;
import info.smart_tools.smartactors.endpoint_components_generic.parse_tree.ParseTree;
import info.smart_tools.smartactors.endpoint_components_netty.default_channel_initialization_handlers.HTTPServerChannelSetupHandler;
import info.smart_tools.smartactors.endpoint_components_netty.default_channel_initialization_handlers.InboundNettyChannelHandlerSetupHandler;
import info.smart_tools.smartactors.endpoint_components_netty.default_channel_initialization_handlers.OutboundChannelCreator;
import info.smart_tools.smartactors.endpoint_components_netty.default_channel_initialization_handlers.StoreOutboundChannelIdToContext;
import info.smart_tools.smartactors.endpoint_components_netty.http_exceptional_action.HttpServerExceptionalAction;
import info.smart_tools.smartactors.endpoint_components_netty.http_headers_setter.HttpHeadersSetter;
import info.smart_tools.smartactors.endpoint_components_netty.http_query_string_parser.HttpQueryStringParser;
import info.smart_tools.smartactors.endpoint_components_netty.http_response_metadata_presetup.HttpResponseMetadataPreSetup;
import info.smart_tools.smartactors.endpoint_components_netty.inbound_netty_channel_handler.InboundNettyChannelHandler;
import info.smart_tools.smartactors.endpoint_components_netty.inbound_netty_message_reference_count_management_components.ReleaseNettyMessageHandler;
import info.smart_tools.smartactors.endpoint_components_netty.inbound_netty_message_reference_count_management_components.RetainNettyMessageHandler;
import info.smart_tools.smartactors.endpoint_components_netty.inbound_netty_message_reference_count_management_components.message_extractors.InboundMessageExtractor;
import info.smart_tools.smartactors.endpoint_components_netty.inbound_netty_message_reference_count_management_components.message_extractors.OutboundMessageExtractor;
import info.smart_tools.smartactors.endpoint_components_netty.inbound_netty_message_reference_count_management_components.message_extractors.WrappedInboundMessageExtractor;
import info.smart_tools.smartactors.endpoint_components_netty.inbound_netty_message_reference_count_management_components.message_extractors.WrappedOutboundMessageExtractor;
import info.smart_tools.smartactors.endpoint_components_netty.outbound_netty_message_creation_handler.OutboundNettyMessageCreationHandler;
import info.smart_tools.smartactors.endpoint_components_netty.send_netty_message_message_handler.SendNettyMessageMessageHandler;
import info.smart_tools.smartactors.endpoint_components_netty.wrap_inbound_netty_message_to_message_byte_array_message_handler.WrapInboundNettyMessageToMessageByteArrayMessageHandler;
import info.smart_tools.smartactors.endpoint_interfaces.iendpoint_pipeline.IEndpointPipeline;
import info.smart_tools.smartactors.endpoint_interfaces.ioutbound_connection_channel.IOutboundConnectionChannel;
import info.smart_tools.smartactors.endpoint_interfaces.ioutbound_connection_channel.IOutboundConnectionChannelListener;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Plugin that registers some {@link info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandler
 * message handler} implementations specific for Netty endpoints and some related strategies.
 */
public class MessageHandlerResolutionStrategiesPlugin extends BootstrapPlugin {
    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public MessageHandlerResolutionStrategiesPlugin(final IBootstrap bootstrap) {
            super(bootstrap);
    }

    @Item("netty_endpoint_message_handlers")
    @After({
            "netty_outbound_message_factories_strategy",
    })
    public void registerHandlerStrategies() throws Exception {
        IFieldName maxAggregatedContentLenFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                "maxAggregatedContentLength");
        IFieldName pipelineFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "pipeline");
        IFieldName messageClassFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "messageClass");
        IFieldName listenerFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "listener");
        IFieldName templatesFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "templates");
        IFieldName messageExtractorFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "messageExtractor");
        IFieldName messageTypeFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "messageType");

        IAdditionDependencyStrategy storage = IOC.resolve(
                Keys.getOrAdd("expandable_strategy#endpoint message handler"));

        storage.register("netty/http cookie setter",
                new SingletonStrategy(new CookiesSetter()));
        storage.register("netty/http headers setter",
                new SingletonStrategy(new HttpHeadersSetter()));

        IOC.register(Keys.getOrAdd(IParseTree.class.getCanonicalName()),
                new ApplyFunctionToArgumentsStrategy(args -> new ParseTree()));

        /*
         * {
         *  "type": "netty/fixed http path parser",
         *  "templates": [
         *      ".. path template ..",
         *      .. more templates ..
         *  ]
         * }
         */
        storage.register("netty/fixed http path parser",
                new MessageHandlerResolutionStrategy((type, handlerConf, endpointConf, pipelineSet) -> {
                    List templates = (List) handlerConf.getValue(templatesFN);

                    if (null == templates) {
                        templates = (List) endpointConf.getValue(templatesFN);
                    }

                    return new HttpPathParse(templates);
                }));

        storage.register("netty/http query string parser",
                new SingletonStrategy(new HttpQueryStringParser()));

        storage.register("netty/http response metadata presetup",
                new SingletonStrategy(new HttpResponseMetadataPreSetup()));

        /*
         * {
         *  "type": "netty/setup/http server channel",
         *  "maxAggregatedContentLength": #max. length of aggregated message#
         * }
         */
        storage.register("netty/setup/http server channel",
                new MessageHandlerResolutionStrategy((type, handlerConf, endpointConf, pipelineSet) -> {
                    int maxAggregatedContentLen = ((Number) handlerConf.getValue(maxAggregatedContentLenFN)).intValue();

                    return new HTTPServerChannelSetupHandler(maxAggregatedContentLen);
                }));

        /*
         * {
         *  "type": "netty/setup/inbound chanel handler",
         *  "pipeline": ".. pipeline name ..",
         *  "messageClass": ".. canonical name of message class .."
         * }
         */
        storage.register("netty/setup/inbound chanel handler",
                new MessageHandlerResolutionStrategy((type, handlerConf, endpointConf, pipelineSet) -> {
                    String pipelineId = (String) handlerConf.getValue(pipelineFN);
                    IEndpointPipeline pipeline = pipelineSet.getPipeline(pipelineId);
                    Class messageClass = getClass().getClassLoader().loadClass((String) handlerConf.getValue(messageClassFN));

                    return new InboundNettyChannelHandlerSetupHandler(
                            new InboundNettyChannelHandler(
                                    pipeline.getInputCallback(),
                                    pipeline.getContextFactory(),
                                    messageClass
                            ));
                }));

        /*
         * {
         *  "type": "netty/setup/attach dynamic outbound channel",
         *  "pipeline": ".. pipeline id ..",
         *  "listener": ".. channel listener dependency name .."
         * }
         */
        storage.register("netty/setup/attach dynamic outbound channel",
                new MessageHandlerResolutionStrategy((type, handlerConf, endpointConf, pipelineSet) -> {
                    String pipelineName = (String) handlerConf.getValue(pipelineFN);
                    IEndpointPipeline pipeline = pipelineSet.getPipeline(pipelineName);
                    IOutboundConnectionChannelListener listener =
                            IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), handlerConf.getValue(listenerFN)), handlerConf);

                    IFunction<Channel, IOutboundConnectionChannel> outboundChannelProvider =
                            ch -> new DefaultOutboundConnectionChannel<>(pipeline, ch);

                    return new OutboundChannelCreator<>(
                            () -> UUID.randomUUID().toString(),
                            outboundChannelProvider,
                            listener
                    );
                }));

        storage.register("netty/store outbound channel id",
                new SingletonStrategy(new StoreOutboundChannelIdToContext<>()));

        SimpleStrictStorageStrategy extractorStorage = new SimpleStrictStorageStrategy(new HashMap<>(), "message extractor");
        extractorStorage.register("unwrapped inbound", new SingletonStrategy(new InboundMessageExtractor()));
        extractorStorage.register("unwrapped outbound", new SingletonStrategy(new OutboundMessageExtractor()));
        extractorStorage.register("wrapped inbound", new SingletonStrategy(new WrappedInboundMessageExtractor()));
        extractorStorage.register("wrapped outbound", new SingletonStrategy(new WrappedOutboundMessageExtractor()));

        IOC.register(Keys.getOrAdd("netty message extractor"), extractorStorage);
        IOC.register(Keys.getOrAdd("expandable_strategy#netty message extractor"),
                new SingletonStrategy(extractorStorage));

        /*
         * {
         *  "type": "netty/retain message",
         *  "messageExtractor": ".. message extractor name .."
         * }
         */
        storage.register("netty/retain message",
                new MessageHandlerResolutionStrategy((type, handlerConf, endpointConf, pipelineSet) -> {
                    String extractorName = (String) handlerConf.getValue(messageExtractorFN);
                    IFunction extractor = IOC.resolve(Keys.getOrAdd("netty message extractor"), extractorName);
                    return new RetainNettyMessageHandler(extractor);
                }));

        /*
         * {
         *  "type": "netty/release message",
         *  "messageExtractor": ".. message extractor name .."
         * }
         */
        storage.register("netty/release message",
                new MessageHandlerResolutionStrategy((type, handlerConf, endpointConf, pipelineSet) -> {
                    String extractorName = (String) handlerConf.getValue(messageExtractorFN);
                    IFunction extractor = IOC.resolve(Keys.getOrAdd("netty message extractor"), extractorName);
                    return new ReleaseNettyMessageHandler(extractor);
                }));

        /*
         * {
         *  "type": "netty/create outbound message",
         *  "messageType": ".. message factory name .."
         * }
         */
        storage.register("netty/create outbound message",
                new MessageHandlerResolutionStrategy((type, handlerConf, endpointConf, pipelineSet) -> {
                    Object typeName = handlerConf.getValue(messageTypeFN);
                    IFunction0 factory = IOC.resolve(Keys.getOrAdd("netty outbound message factory"), typeName);
                    return new OutboundNettyMessageCreationHandler(factory);
                }));

        storage.register("netty/send outbound message",
                new SingletonStrategy(new SendNettyMessageMessageHandler()));
        storage.register("netty/wrap inbound message",
                new SingletonStrategy(new WrapInboundNettyMessageToMessageByteArrayMessageHandler()));
    }

    @Item("netty_exceptional_actions")
    public void registerExceptionalActions() throws Exception {
        IOC.register(Keys.getOrAdd("netty http server default exceptional action"),
                new SingletonStrategy(new HttpServerExceptionalAction()));
    }

    @Item("netty_outbound_message_factories_strategy")
    public void registerOutboundMessageFactories() throws Exception {
        SimpleStrictStorageStrategy storage = new SimpleStrictStorageStrategy("outbound netty message");

        IOC.register(Keys.getOrAdd("netty outbound message factory"), storage);
        IOC.register(Keys.getOrAdd("expandable_strategy#netty outbound message factory"),
                new SingletonStrategy(storage));
    }

    @Item("netty_default_outbound_message_factories")
    @After({
            "netty_outbound_message_factories_strategy"
    })
    public void registerDefaultOutboundNettyMessageFactories() throws Exception {
        IAdditionDependencyStrategy storage = IOC.resolve(Keys.getOrAdd("expandable_strategy#netty outbound message factory"));

        storage.register("http response", new SingletonStrategy(
                (IFunction0) () -> new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)));
    }
}
