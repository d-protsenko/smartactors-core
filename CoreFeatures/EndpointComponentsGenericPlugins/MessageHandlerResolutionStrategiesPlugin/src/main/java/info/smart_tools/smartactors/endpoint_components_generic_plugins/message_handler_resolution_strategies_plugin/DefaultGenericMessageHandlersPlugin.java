package info.smart_tools.smartactors.endpoint_components_generic_plugins.message_handler_resolution_strategies_plugin;

import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.IAdditionDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iaction.IBiAction;
import info.smart_tools.smartactors.base.interfaces.iaction.IFunction;
import info.smart_tools.smartactors.base.simple_strict_storage_strategy.SimpleStrictStorageStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.endpoint_components_generic.asynchronous_unordered_message_handler.AsynchronousUnorderedMessageHandler;
import info.smart_tools.smartactors.endpoint_components_generic.create_empty_message_message_handler.CreateEmptyMessageMessageHandler;
import info.smart_tools.smartactors.endpoint_components_generic.create_environment_message_handler.CreateEnvironmentMessageHandler;
import info.smart_tools.smartactors.endpoint_components_generic.dead_end_message_handler.DeadEndMessageHandler;
import info.smart_tools.smartactors.endpoint_components_generic.default_message_codecs.impl.json.JsonBlockDecoder;
import info.smart_tools.smartactors.endpoint_components_generic.default_message_codecs.impl.json.JsonBlockEncoder;
import info.smart_tools.smartactors.endpoint_components_generic.endpoint_response_strategy.EndpointResponseStrategy;
import info.smart_tools.smartactors.endpoint_components_generic.error_message_handler.ErrorMessageHandler;
import info.smart_tools.smartactors.endpoint_components_generic.generic_exception_interceptor_message_handler.GenericExceptionInterceptorMessageHandler;
import info.smart_tools.smartactors.endpoint_components_generic.message_attribute_routing_message_handler.MessageAttributeRoutingMessageHandler;
import info.smart_tools.smartactors.endpoint_components_generic.message_handler_resolution_strategy.MessageHandlerResolutionStrategy;
import info.smart_tools.smartactors.endpoint_components_generic.response_strategy_set_message_handler.ResponseStrategySetMessageHandler;
import info.smart_tools.smartactors.endpoint_components_generic.scope_setter_message_handler.ScopeSetterMessageHandler;
import info.smart_tools.smartactors.endpoint_components_generic.send_internal_message_handler.SendInternalMessageHandler;
import info.smart_tools.smartactors.endpoint_interfaces.iendpoint_pipeline.IEndpointPipeline;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandler;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Plugin registering strategies for resolution of some of {@link IMessageHandler message handlers} from
 * {@code EndpointComponentsGeneric} feature.
 */
public class DefaultGenericMessageHandlersPlugin extends BootstrapPlugin {
    /**
     * The constructor.
     *
     * @param bootstrap the bootstrap
     */
    public DefaultGenericMessageHandlersPlugin(final IBootstrap bootstrap) {
        super(bootstrap);
    }

    @Item("default_generic_message_handlers")
    @After({
            "base_message_handler_strategies",
            "global_message_handler_tables_storage",
    })
    public void registerDefaultHandlers() throws Exception {
        IFieldName actionFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "action");
        IFieldName extractorFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "extractor");
        IFieldName defaultFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "default");
        IFieldName routesFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "routes");
        IFieldName valueFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "value");
        IFieldName tableFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "table");
        IFieldName pipelineFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "pipeline");
        IFieldName stackDepthFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "stackDepth");
        IFieldName chainFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "chain");
        IFieldName messageFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "message");

        IAdditionDependencyStrategy storage = IOC.resolve(Keys.getOrAdd("expandable_strategy#endpoint message handler"));

        storage.register("default async unordered executor",
                new MessageHandlerResolutionStrategy((type, handlerConf, endpointConf, pipelineSet) -> {
                    IQueue<ITask> taskQueue = IOC.resolve(Keys.getOrAdd("task_queue"));
                    return new AsynchronousUnorderedMessageHandler(taskQueue);
        }));
        storage.register("create empty message",
                new SingletonStrategy(new CreateEmptyMessageMessageHandler()));
        storage.register("create environment",
                new SingletonStrategy(new CreateEnvironmentMessageHandler()));
        storage.register("dead end",
                new SingletonStrategy(new DeadEndMessageHandler()));
        storage.register("encoder/block/json",
                new SingletonStrategy(new JsonBlockEncoder()));
        storage.register("decoder/block/json",
                new SingletonStrategy(new JsonBlockDecoder()));

        /*
         * {
         *  "type": "exception interceptor",
         *  "action": ".. action dependency name .."
         * }
         */
        storage.register("exception interceptor",
                new MessageHandlerResolutionStrategy((type, handlerConf, endpointConf, pipelineSet) -> {
                    Object actionName = handlerConf.getValue(actionFN);
                    IBiAction action = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), actionName), handlerConf);
                    return new GenericExceptionInterceptorMessageHandler(action);
                }));

        SimpleStrictStorageStrategy attributeExtractorsStorage
                = new SimpleStrictStorageStrategy("message attribute extractor");
        IOC.register(Keys.getOrAdd("message attribute extractor"), attributeExtractorsStorage);
        IOC.register(Keys.getOrAdd("expandable_strategy#message attribute extractor"),
                new SingletonStrategy(attributeExtractorsStorage));

        /*
         * {
         *  "type": "fixed attribute router",
         *  "extractor": ".. attribute extractor name ..",
         *  "default": {
         *      "type": ".. handler type name ..",
         *      .. handler config ..
         *  },
         *  "routes": [
         *      {
         *          "value": ".. expected attribute value ..",
         *          "type": ".. handler type name ..",
         *          .. handler config ..
         *      },
         *      .. more handlers ..
         *  ]
         * }
         */
        storage.register("fixed attribute router",
                new MessageHandlerResolutionStrategy((type, handlerConf, endpointConf, pipelineSet) -> {
                    Object extractorName = handlerConf.getValue(extractorFN);
                    IFunction extractor = IOC.resolve(
                            Keys.getOrAdd("message attribute extractor"),
                            extractorName, handlerConf);

                    IMessageHandler defaultHandler = IOC.resolve(Keys.getOrAdd("parse endpoint message handler"),
                            handlerConf.getValue(defaultFN), endpointConf, pipelineSet);

                    Map routes = new HashMap();

                    List routeConfigs = (List) handlerConf.getValue(routesFN);

                    for (Object routeConf : routeConfigs) {
                        IObject routeConfIObj = (IObject) routeConf;

                        routes.put(
                                routeConfIObj.getValue(valueFN),
                                IOC.resolve(Keys.getOrAdd("parse endpoint message handler"),
                                        routeConfIObj, endpointConf, pipelineSet)
                        );
                    }

                    return new MessageAttributeRoutingMessageHandler(extractor, routes, defaultHandler);
                }));

        /*
         * {
         *  "type": "global table attribute router",
         *  "extractor": ".. attribute extractor name ..",
         *  "default": {
         *      "type": ".. handler type name ..",
         *      .. handler config ..
         *  },
         *  "table": ".. global table dependency name .."
         * }
         */
        storage.register("global table attribute router",
                new MessageHandlerResolutionStrategy((type, handlerConf, endpointConf, pipelineSet) -> {
                    Object extractorName = handlerConf.getValue(extractorFN);
                    IFunction extractor = IOC.resolve(
                            Keys.getOrAdd("message attribute extractor"),
                            extractorName, handlerConf);

                    IMessageHandler defaultHandler = IOC.resolve(Keys.getOrAdd("parse endpoint message handler"),
                            handlerConf.getValue(defaultFN), endpointConf, pipelineSet);

                    Map routes = IOC.resolve(
                            Keys.getOrAdd("message handler table"),
                            handlerConf.getValue(tableFN), handlerConf);

                    return new MessageAttributeRoutingMessageHandler(extractor, routes, defaultHandler);
                }));

        /*
         * {
         *  "type": "response strategy setter",
         *  "pipeline": ".. response pipeline name .."
         * }
         */
        storage.register("response strategy setter",
                new MessageHandlerResolutionStrategy((type, handlerConf, endpointConf, pipelineSet) -> {
                    String pipelineName = (String) handlerConf.getValue(pipelineFN);
                    IEndpointPipeline pipeline = pipelineSet.getPipeline(pipelineName);

                    return new ResponseStrategySetMessageHandler(
                            new EndpointResponseStrategy(pipeline.getInputCallback(), pipeline.getContextFactory())
                    );
                }));

        storage.register("default scope setter",
                new MessageHandlerResolutionStrategy((type, handlerConf, endpointConf, pipelineSet) -> {
                    IScope scope = ScopeProvider.getCurrentScope();
                    return new ScopeSetterMessageHandler(scope);
                }));

        /*
         * {
         *  "type": "internal message sender",
         *  "stackDepth": #MPS stack depth#,
         *  "chain": ".. initial message chain .."
         * }
         */
        storage.register("internal message sender",
                new MessageHandlerResolutionStrategy((type, handlerConf, endpointConf, pipelineSet) -> {
                    int stackDepth = ((Number) handlerConf.getValue(stackDepthFN)).intValue();

                    Object chainName = handlerConf.getValue(chainFN);
                    Object chainId = IOC.resolve(Keys.getOrAdd("chain_id_from_map_name"), chainName);
                    IChainStorage chainStorage = IOC.resolve(Keys.getOrAdd(IChainStorage.class.getCanonicalName()));
                    IReceiverChain chain = chainStorage.resolve(chainId);

                    IQueue<ITask> taskQueue = IOC.resolve(Keys.getOrAdd("task_queue"));

                    return new SendInternalMessageHandler(stackDepth, taskQueue, chain);
                }));

        /*
         * {
         *  "type": "error",
         *  "message": ".. error message .."
         * }
         */
        storage.register("error",
                new MessageHandlerResolutionStrategy((type, handlerConf, endpointConf, pipelineSet) -> {
                    String message = (String) handlerConf.getValue(messageFN);

                    return new ErrorMessageHandler(message);
                }));
    }

    @Item("global_message_handler_tables_storage")
    public void registerTableStorage() throws Exception {
        SimpleStrictStorageStrategy tableStorage = new SimpleStrictStorageStrategy("message handler table");
        IOC.register(Keys.getOrAdd("message handler table"), tableStorage);
        IOC.register(Keys.getOrAdd("expandable_strategy#message handler table"),
                new SingletonStrategy(tableStorage));
    }
}
