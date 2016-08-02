package info.smart_tools.smartactors.plugin.configuration_manager;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.configuration_manager.ConfigurationManager;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iconfiguration_manager.IConfigurationManager;
import info.smart_tools.smartactors.core.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.invalid_state_exception.InvalidStateException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;

/**
 *
 */
public class PluginConfigurationManager implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public PluginConfigurationManager(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            // "configuration_manager" - create and register in IOC instance of ConfigurationManager
            IBootstrapItem<String> configurationManagerItem = new BootstrapItem("configuration_manager");

            configurationManagerItem
                    .after("IOC")
                    .process(() -> {
                        try {
                            IOC.register(Keys.getOrAdd(IConfigurationManager.class.getCanonicalName()),
                                    new SingletonStrategy(new ConfigurationManager()));
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException("ConfigurationManager plugin can't load: can't get ConfigurationManager key", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecuteException("ConfigurationManager plugin can't load: can't create strategy", e);
                        } catch (RegistrationException e) {
                            throw new ActionExecuteException("ConfigurationManager plugin can't load: can't register new strategy", e);
                        }
                    });

            bootstrap.add(configurationManagerItem);

            // "configure" - call #configure() method of ConfigurationManager instance created by "configuration_manager" item
            IBootstrapItem<String> configureItem = new BootstrapItem("configure");

            configureItem
                    .after("configuration_manager")
                    .process(() -> {
                        try {
                            IConfigurationManager configurationManager = IOC.resolve(
                                    Keys.getOrAdd(IConfigurationManager.class.getCanonicalName()));

                            configurationManager.configure();
                        } catch (ResolutionException | InvalidStateException | ConfigurationProcessingException e) {
                            throw new ActionExecuteException("ConfigurationManager plugin can't load", e);
                        }
                    });

            bootstrap.add(configureItem);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
