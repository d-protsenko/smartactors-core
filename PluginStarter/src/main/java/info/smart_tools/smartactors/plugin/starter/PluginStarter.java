package info.smart_tools.smartactors.plugin.starter;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iconfiguration_manager.IConfigurationManager;
import info.smart_tools.smartactors.core.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.exception.invalid_state_exception.InvalidStateException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

/**
 * Implementation of {@link IPlugin}.
 *
 */
public class PluginStarter implements IPlugin {

    /** Local storage for instance of {@link IBootstrap}*/
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * @param bootstrap Target bootstrap for adding strategy
     * @throws InvalidArgumentException if any errors occurred
     */
    public PluginStarter(final IBootstrap<IBootstrapItem<String>> bootstrap)
            throws InvalidArgumentException {
        if (null == bootstrap) {
            throw new InvalidArgumentException("Incoming argument should not be null.");
        }
        this.bootstrap = bootstrap;
    }

    @Override
    public void load()
            throws PluginException {
        try {
            IBootstrapItem<String> configureItem = new BootstrapItem("starter");
            configureItem
                    .process(() -> {
                        try {
                            IConfigurationManager configurationManager = IOC.resolve(
                                    Keys.getOrAdd(IConfigurationManager.class.getCanonicalName()));

                            configurationManager.configure();
                        } catch (ResolutionException | InvalidStateException | ConfigurationProcessingException e) {
                            throw new ActionExecuteException(e);
                        }
                    });

            bootstrap.add(configureItem);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
