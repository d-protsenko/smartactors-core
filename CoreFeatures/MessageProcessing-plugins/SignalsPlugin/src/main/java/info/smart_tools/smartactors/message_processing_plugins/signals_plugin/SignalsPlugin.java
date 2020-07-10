package info.smart_tools.smartactors.message_processing_plugins.signals_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.message_processing.signals.AbortSignal;
import info.smart_tools.smartactors.message_processing.signals.ShutdownSignal;

/**
 * Registers signal types defined in {@link info.smart_tools.smartactors.message_processing.signals}.
 */
public class SignalsPlugin extends BootstrapPlugin {

    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public SignalsPlugin(final IBootstrap bootstrap) {
            super(bootstrap);
    }

    @Item("system_signal_classes")
    @After({
        "IOC",
        "IFieldNamePlugin",
    })
    public void registerSystemSignals()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(Keys.getKeyByName("shutdown signal"), new SingletonStrategy(new ShutdownSignal()));
        IOC.register(Keys.getKeyByName("abort signal"), new SingletonStrategy(new AbortSignal()));
    }

    @ItemRevert("system_signal_classes")
    public void unregisterSystemSignals() {
        String[] itemNames = {
                "shutdown signal",
                "abort signal"
        };
        Keys.unregisterByNames(itemNames);
    }
}
