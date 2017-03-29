package info.smart_tools.smartactors.message_processing_plugins.object_creation_strategies_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.FunctionExecutionException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing.actor_receiver.ActorReceiver;
import info.smart_tools.smartactors.message_processing.handler_routing_receiver.HandlerRoutingReceiver;
import info.smart_tools.smartactors.message_processing.object_creation_strategies.FullObjectCreatorResolutionStrategy;
import info.smart_tools.smartactors.message_processing.object_creation_strategies.MethodInvokerReceiverResolutionStrategy;
import info.smart_tools.smartactors.message_processing.object_creation_strategies.RouterRegistrationObjectListener;
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
    @After("router")
    public void registerRouterListener()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(
                Keys.getOrAdd("global router registration receiver object listener"),
                new SingletonStrategy(new RouterRegistrationObjectListener())
        );
    }

    @Item("full_object_creator_resolution_strategy")
    public void registerFullCreatorStrategy()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(
                Keys.getOrAdd("full receiver object creator"),
                new FullObjectCreatorResolutionStrategy()
        );
    }

    @Item("basic_receiver_strategies")
    public void registerBasicReceiver()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        // Dependencies of ActorReceiver
        IOC.register(
                Keys.getOrAdd("actor_receiver_queue"),
                new ApplyFunctionToArgumentsStrategy(args -> new ConcurrentLinkedQueue()));
        IOC.register(
                Keys.getOrAdd("actor_receiver_busyness_flag"),
                new ApplyFunctionToArgumentsStrategy(args -> new AtomicBoolean(false)));

        IOC.register(
                Keys.getOrAdd("create actor synchronization receiver"),
                new ApplyFunctionToArgumentsStrategy(args -> {
                    try {
                        return new ActorReceiver((IMessageReceiver) args[0]);
                    } catch (InvalidArgumentException | ResolutionException e) {
                        throw new FunctionExecutionException(e);
                    }
                })
        );

        IOC.register(
                Keys.getOrAdd("create handler router receiver"),
                new ApplyFunctionToArgumentsStrategy(args -> {
                    try {
                        return new HandlerRoutingReceiver((Map) args[0]);
                    } catch (InvalidArgumentException | ResolutionException e) {
                        throw new FunctionExecutionException(e);
                    }
                })
        );
    }

    @Item("invoker_receiver_creation_strategy")
    public void registerInvokerCreationStrategy()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(
                Keys.getOrAdd("method invoker receiver"),
                new MethodInvokerReceiverResolutionStrategy()
        );
    }
}
