package info.smart_tools.smartactors.message_processing_plugins.object_creation_strategies_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing.object_creation_strategies.FullObjectCreatorResolutionStrategy;
import info.smart_tools.smartactors.message_processing.object_creation_strategies.MethodInvokerReceiverResolutionStrategy;
import info.smart_tools.smartactors.message_processing.object_creation_strategies.RouterRegistrationObjectListener;

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

    @Item("invoker_receiver_creation_strategy")
    public void registerInvokerCreationStrategy()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(
                Keys.getOrAdd("method invoker receiver"),
                new MethodInvokerReceiverResolutionStrategy()
        );
    }
}
