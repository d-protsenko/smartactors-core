package info.smart_tools.smartactors.database_service_starter.database_starter;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.IConfigurationManager;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

public class PluginDatabaseConfigSection implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * The constructor.
     *
     * @param bootstrap the bootstrap
     */
    public PluginDatabaseConfigSection(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            /* "database" section */
            IBootstrapItem<String> endpointsSectionItem = new BootstrapItem("config_section:database");

            endpointsSectionItem
                    .before("config_section:objects")
                    .process(() -> {
                        try {
                            IConfigurationManager configurationManager =
                                    IOC.resolve(Keys.getOrAdd(IConfigurationManager.class.getCanonicalName()));
                            configurationManager.addSectionStrategy(new DatabaseSectionStrategy());
                        } catch (ResolutionException | InvalidArgumentException e) {
                            throw new ActionExecuteException(e);
                        }
                    });

            bootstrap.add(endpointsSectionItem);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
