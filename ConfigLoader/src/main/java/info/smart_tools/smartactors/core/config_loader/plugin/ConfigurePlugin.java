package info.smart_tools.smartactors.core.config_loader.plugin;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.config_loader.ISectionStrategiesStorage;
import info.smart_tools.smartactors.core.config_loader.impl.SectionStrategiesStorage;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;

/**
 *
 */
public class ConfigurePlugin implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public ConfigurePlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            /* config_loader - register ISectionStrategiesStorage singleton in IOC */
            IBootstrapItem<String> configLoaderItem = new BootstrapItem("config_loader");

            configLoaderItem
                    .after("ioc")
                    .process(() -> {
                        try {
                            IOC.register(IOC.resolve(IOC.getKeyForKeyStorage(), ISectionStrategiesStorage.class.getCanonicalName()),
                                    new SingletonStrategy(new SectionStrategiesStorage()));
                        } catch (InvalidArgumentException | ResolutionException | RegistrationException e) {
                            throw new ActionExecuteException(e);
                        }
                    });

            bootstrap.add(configLoaderItem);

            /* configure - loads initial configuration */
            IBootstrapItem<String> configureItem = new BootstrapItem("configure");

            configureItem
                    .after("config_loader")
                    .process(() -> {
                        // TODO: Load initial configuration and parse it using ISectionStrategy's
                    });

            bootstrap.add(configureItem);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
