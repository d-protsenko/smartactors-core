package info.smart_tools.smartactors.plugin.receiver_generator;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.ireceiver_generator.IReceiverGenerator;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.receiver_generator.ReceiverGenerator;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;

/**
 * Plugin creates instance of {@link ReceiverGenerator} and registers it into IOC.
 */
public class InitializeReceiverGenerator implements IPlugin {

    /** Local storage for instance of {@link IBootstrap}*/
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor with single argument
     * @param bootstrap instance of {@link IBootstrap}
     * @throws InvalidArgumentException if any errors occurred
     */
    public InitializeReceiverGenerator(final IBootstrap<IBootstrapItem<String>> bootstrap)
            throws InvalidArgumentException {
        if (null == bootstrap) {
            throw new InvalidArgumentException("Incoming argument should not be null.");
        }
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("InitializeReceiverGenerator");
            item
                    .after("IOC")
                    .process(
                            () -> {
                                try {
                                    IReceiverGenerator rg = new ReceiverGenerator(this.getClass().getClassLoader());
                                    IOC.register(
                                            Keys.getOrAdd(IReceiverGenerator.class.getCanonicalName()),
                                            new SingletonStrategy(rg)
                                    );
                                } catch (ResolutionException e) {
                                    throw new ActionExecuteException("InitializeReceiverGenerator plugin can't load: can't get InitializeReceiverGenerator key", e);
                                } catch (InvalidArgumentException e) {
                                    throw new ActionExecuteException("InitializeReceiverGenerator plugin can't load: can't create strategy", e);
                                } catch (RegistrationException e) {
                                    throw new ActionExecuteException("InitializeReceiverGenerator plugin can't load: can't register new strategy", e);
                                }
                            }
                        );
            this.bootstrap.add(item);
        } catch (Throwable e) {
            throw new PluginException("Could not load 'ReceiverGenerator plugin'", e);
        }
    }
}
