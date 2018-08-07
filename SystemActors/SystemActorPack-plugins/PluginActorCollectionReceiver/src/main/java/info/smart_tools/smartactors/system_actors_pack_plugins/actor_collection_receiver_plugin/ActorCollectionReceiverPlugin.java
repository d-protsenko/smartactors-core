package info.smart_tools.smartactors.system_actors_pack_plugins.actor_collection_receiver_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.IAdditionDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.exception.AdditionDependencyStrategyException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.FunctionExecutionException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.system_actors_pack.actor_collection_receiver.ActorCollectionReceiver;
import info.smart_tools.smartactors.system_actors_pack.actor_collection_receiver.pipeline.ChildDeletionCheckerReceiver;
import info.smart_tools.smartactors.system_actors_pack.actor_collection_receiver.strategies.DefaultDeletionCheckStrategy;
import info.smart_tools.smartactors.system_actors_pack.actor_collection_receiver_interfaces.IChildDeletionCheckStrategy;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plugin registers into IOC strategy for creation new instance of {@link ActorCollectionReceiver}.
 */
public class ActorCollectionReceiverPlugin extends BootstrapPlugin {

    /**
     * The constructor.
     *
     * @param bootstrap the bootstrap
     */
    public ActorCollectionReceiverPlugin(final IBootstrap bootstrap) {
        super(bootstrap);
    }

    @Item("actor_collection_receiver_config_canonization_strategies")
    public void registerConfigCanonizationStrategies()
            throws ResolutionException, RegistrationException, InvalidArgumentException, AdditionDependencyStrategyException {
        IFieldName kindFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "kind");

        IAdditionDependencyStrategy strategy = IOC.resolve(Keys.getOrAdd("expandable_strategy#resolve key for configuration object"));

        strategy.register("new", new ApplyFunctionToArgumentsStrategy(args -> {
            try {
                Object value = args[1];

                if (!(value instanceof IObject)) {
                    return value;
                }

                String kindName = (String) ((IObject) value).getValue(kindFieldName);

                if (null != kindName && !kindName.startsWith("child_")) {
                    ((IObject) value).setValue(kindFieldName, "child_" + kindName);
                }

                return IOC.resolve(Keys.getOrAdd("canonize objects configuration section item filters list"), value);
            } catch (ResolutionException | ReadValueException | ChangeValueException | InvalidArgumentException e) {
                throw new FunctionExecutionException(e);
            }
        }));
    }

    @Item("actor_collection_receiver_default_child_deletion_check_strategy")
    public void registerDefailtDeletionCheckStrategy()
            throws ResolutionException, InvalidArgumentException, RegistrationException {
        IOC.register(Keys.getOrAdd("default child deletion check strategy"),
                new SingletonStrategy(new DefaultDeletionCheckStrategy()));
    }
    
    @Item("actor_collection_receiver_child_receiver_deletion_check_receiver_decorator_strategy")
    @After({
        "actor_collection_receiver_default_child_deletion_check_strategy",
    })
    public void registerDeletionCheckerDecorator()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IFieldName deletionCheckStrategyFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "deletionCheckStrategy");
        IFieldName deletionActionFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "deletionAction");

        IOC.register(Keys.getOrAdd("child deletion checker receiver decorator"),
                new ApplyFunctionToArgumentsStrategy(args -> {
                    IMessageReceiver receiver = (IMessageReceiver) args[0];
                    IObject objectConfig = (IObject) args[2];
                    IObject context = (IObject) args[3];

                    try {
                        Object deletionCheckStrategyKeyName = objectConfig.getValue(deletionCheckStrategyFN);

                        if (null == deletionCheckStrategyKeyName) {
                            deletionCheckStrategyKeyName = "default child deletion check strategy";
                        }

                        IChildDeletionCheckStrategy deletionCheckStrategy = IOC.resolve(
                                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), deletionCheckStrategyKeyName),
                                objectConfig
                        );

                        IAction<IObject> deletionAction = (IAction<IObject>) context.getValue(deletionActionFN);

                        return new ChildDeletionCheckerReceiver(receiver, context, deletionCheckStrategy, deletionAction);
                    } catch (Exception e) {
                        throw new FunctionExecutionException(e);
                    }
                }));
    }

    @Item("actor_collection_receiver_pipeline_configuration_definitions")
    @After({
        "actor_collection_receiver_child_receiver_deletion_check_receiver_decorator_strategy",
        "actor_collection_receiver_config_canonization_strategies",
    })
    public void registerPipelineConfiguration()
            throws ResolutionException, RegistrationException, ChangeValueException, InvalidArgumentException {
        IObject childDeletionCheckerConfig = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        childDeletionCheckerConfig.setValue(
                IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "dependency"),
                "filter creator#decorate receiver");
        childDeletionCheckerConfig.setValue(
                IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "decoratorDependency"),
                "child deletion checker receiver decorator");
        IOC.register(
                Keys.getOrAdd("named filter config#child receiver deletion checker"),
                new SingletonStrategy(childDeletionCheckerConfig)
        );

        IOC.register(
                Keys.getOrAdd("object kind filter sequence#child_raw"),
                new SingletonStrategy(Arrays.asList(
                        "top-level object",
                        "child receiver deletion checker",
                        "set address from name"
                ))
        );
        IOC.register(
                Keys.getOrAdd("object kind filter sequence#child_stateless_actor"),
                new SingletonStrategy(Arrays.asList(
                        "top-level object",
                        "method invokers",
                        "handler router receiver",
                        "child receiver deletion checker",
                        "set address from name"
                ))
        );
        IOC.register(
                Keys.getOrAdd("object kind filter sequence#child_actor"),
                new SingletonStrategy(Arrays.asList(
                        "top-level object",
                        "method invokers",
                        "handler router receiver",
                        "child receiver deletion checker",
                        "per-receiver actor sync",
                        "set address from name"
                ))
        );
    }

    @Item("ActorCollectionReceiver")
    @After({
            "actor_collection_receiver_config_canonization_strategies",
            "actor_collection_receiver_pipeline_configuration_definitions",
    })
    public void registerCollectionReceiver()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(
                Keys.getOrAdd("ActorCollection"),
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            try {
                                Map<Object, IMessageReceiver> childStorage = new ConcurrentHashMap<>();
                                return new ActorCollectionReceiver(childStorage);
                            } catch (Exception e) {
                                throw new FunctionExecutionException("Could not create new instance of ActorCollectionReceiver.", e);
                            }
                        }
                )
        );
    }
}
