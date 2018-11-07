package info.smart_tools.smartactors.message_processing_plugins.object_creation_strategies_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.FunctionExecutionException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;
import info.smart_tools.smartactors.message_processing.actor_receiver.ActorReceiver;
import info.smart_tools.smartactors.message_processing.handler_routing_receiver.HandlerRoutingReceiver;
import info.smart_tools.smartactors.message_processing.object_creation_strategies.FullObjectCreatorResolutionStrategy;
import info.smart_tools.smartactors.message_processing.object_creation_strategies.MethodInvokerReceiverResolutionStrategy;
import info.smart_tools.smartactors.message_processing.object_creation_strategies.RouterRegistrationObjectListener;
import info.smart_tools.smartactors.message_processing_interfaces.iwrapper_generator.IWrapperGenerator;
import info.smart_tools.smartactors.message_processing_interfaces.iwrapper_generator.exception.WrapperGeneratorException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ObjectCreationStrategiesPlugin extends BootstrapPlugin {
    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public ObjectCreationStrategiesPlugin(final IBootstrap bootstrap) {
            super(bootstrap);
    }

    @Item("global_router_registration_receiver_object_listener")
    @After({
        "router",
        "IOC",
    })
    @Before({
            "object_creation_strategies:done",
    })
    public void registerRouterListener()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(
                Keys.resolveByName("global router registration receiver object listener"),
                new SingletonStrategy(new RouterRegistrationObjectListener())
        );
    }

    @ItemRevert("global_router_registration_receiver_object_listener")
    public void unregisterRouterListener() {
        try {
            IOC.remove(Keys.resolveByName("global router registration receiver object listener"));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \"global router registration receiver object listener\" has failed while reverting \"global_router_registration_receiver_object_listener\" plugin.");
        } catch (ResolutionException e) { }
    }

    @Item("full_object_creator_resolution_strategy")
    @After({
            "IOC",
    })
    @Before({
            "object_creation_strategies:done",
    })
    public void registerFullCreatorStrategy()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(
                Keys.resolveByName("full receiver object creator"),
                new FullObjectCreatorResolutionStrategy()
        );
    }

    @ItemRevert("full_object_creator_resolution_strategy")
    public void unregisterFullCreatorStrategy() {
        try {
            IOC.remove(Keys.resolveByName("full receiver object creator"));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \"full receiver object creator\" has failed while reverting \"full_object_creator_resolution_strategy\" plugin.");
        } catch (ResolutionException e) { }
    }

    @Item("basic_receiver_strategies")
    @After({
        "IOC",
    })
    public void registerBasicReceiver()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        // Dependencies of ActorReceiver
        IOC.register(
                Keys.resolveByName("actor_receiver_queue"),
                new ApplyFunctionToArgumentsStrategy(args -> new ConcurrentLinkedQueue()));
        IOC.register(
                Keys.resolveByName("actor_receiver_busyness_flag"),
                new ApplyFunctionToArgumentsStrategy(args -> new AtomicBoolean(false)));

        IOC.register(
                Keys.resolveByName("create actor synchronization receiver"),
                new ApplyFunctionToArgumentsStrategy(args -> {
                    try {
                        return new ActorReceiver((IMessageReceiver) args[0]);
                    } catch (InvalidArgumentException | ResolutionException e) {
                        throw new FunctionExecutionException(e);
                    }
                })
        );

        IOC.register(
                Keys.resolveByName("create handler router receiver"),
                new ApplyFunctionToArgumentsStrategy(args -> {
                    try {
                        return new HandlerRoutingReceiver((Map) args[0]);
                    } catch (InvalidArgumentException | ResolutionException e) {
                        throw new FunctionExecutionException(e);
                    }
                })
        );
    }

    @ItemRevert("basic_receiver_strategies")
    public void unregisterBasicReceiver() {
        String itemName = "basic_receiver_strategies";
        String keyName = "";

        try {
            keyName = "actor_receiver_queue";
            IOC.remove(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }

        try {
            keyName = "actor_receiver_busyness_flag";
            IOC.remove(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }

        try {
            keyName = "create actor synchronization receiver";
            IOC.remove(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }

        try {
            keyName = "create handler router receiver";
            IOC.remove(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }
    }

    @Item("wrapper_resolution_strategies_for_invokers")
    @After({
            "IOC",
    })
    public void registerWrapperResolutionStrategies()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IResolveDependencyStrategy newInstanceWrapperStrategy = new ApplyFunctionToArgumentsStrategy(args -> {
            Class<?> clazz = (Class) args[0];

            try {
                IWrapperGenerator wrapperGenerator = IOC.resolve(Keys.resolveByName(IWrapperGenerator.class.getCanonicalName()));

                Object wrapper = wrapperGenerator.generate(clazz);

                return new ApplyFunctionToArgumentsStrategy(args1 -> {
                    try {
                        return wrapper.getClass().newInstance();
                    } catch (Exception e) {
                        throw new FunctionExecutionException(e);
                    }
                });
            } catch (ResolutionException | WrapperGeneratorException e) {
                throw new FunctionExecutionException(e);
            }
        });

        IResolveDependencyStrategy singletonWrapperStrategy = new ApplyFunctionToArgumentsStrategy(args -> {
            Class<?> clazz = (Class) args[0];

            try {
                IWrapperGenerator wrapperGenerator = IOC.resolve(Keys.resolveByName(IWrapperGenerator.class.getCanonicalName()));

                Object wrapper = wrapperGenerator.generate(clazz);

                return new SingletonStrategy(wrapper);
            } catch (ResolutionException | WrapperGeneratorException e) {
                throw new FunctionExecutionException(e);
            }
        });

        IOC.register(
                Keys.resolveByName("default wrapper resolution strategy dependency for invoker receiver"),
                newInstanceWrapperStrategy
        );

        IOC.register(
                Keys.resolveByName("new instance wrapper resolution strategy"),
                newInstanceWrapperStrategy
        );

        IOC.register(
                Keys.resolveByName("singleton wrapper resolution strategy"),
                singletonWrapperStrategy
        );
    }

    @ItemRevert("wrapper_resolution_strategies_for_invokers")
    public void unregisterWrapperResolutionStrategies() {
        String itemName = "wrapper_resolution_strategies_for_invokers";
        String keyName = "";

        try {
            keyName = "singleton wrapper resolution strategy";
            IOC.remove(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }

        try {
            keyName = "new instance wrapper resolution strategy";
            IOC.remove(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }

        try {
            keyName = "default wrapper resolution strategy dependency for invoker receiver";
            IOC.remove(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }
    }

    @Item("invoker_receiver_creation_strategy")
    @After({
        "wrapper_resolution_strategies_for_invokers",
        "IFieldNamePlugin",
        "InitializeReceiverGenerator",
    })
    @Before({
        "object_creation_strategies:done",
    })
    public void registerInvokerCreationStrategy()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(
                Keys.resolveByName("method invoker receiver"),
                new MethodInvokerReceiverResolutionStrategy()
        );
    }

    @ItemRevert("invoker_receiver_creation_strategy")
    public void unregisterInvokerCreationStrategy() {
        String itemName = "invoker_receiver_creation_strategy";
        String keyName = "";

        try {
            keyName = "method invoker receiver";
            IOC.remove(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }
    }

    @Item("object_creation_strategies:done")
    public void creationStrategiesDone() {
    }

    @ItemRevert("object_creation_strategies:done")
    public void destructionStrategiesDone() {
    }
}
