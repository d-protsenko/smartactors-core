package info.smart_tools.smartactors.core.standard_config_sections;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.config_loader.ISectionStrategiesStorage;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;

/**
 *
 */
public class StandardConfigSectionsPlugin implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public StandardConfigSectionsPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            /* "objects" section */
            IBootstrapItem<String> objectsSectionItem = new BootstrapItem("config_section:objects");

            objectsSectionItem
                    .after("config_loader")
                    .after("router_registration")
                    .before("configure")
                    .process(() -> {
                        try {
                            ISectionStrategiesStorage strategiesStorage =
                                    IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), ISectionStrategiesStorage.class.getCanonicalName()));

                            strategiesStorage.register(new ObjectsSectionProcessingStrategy());
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException(e);
                        }
                    });

            bootstrap.add(objectsSectionItem);

            /* "maps" section */
            IBootstrapItem<String> mapsSectionItem = new BootstrapItem("config_section:maps");

            mapsSectionItem
                    .after("config_loader")
                    .after("receiver_chains_storage")
                    .after("receiver_chain")
                    .before("configure")
                    .process(() -> {
                        try {
                            ISectionStrategiesStorage strategiesStorage =
                                    IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), ISectionStrategiesStorage.class.getCanonicalName()));

                            strategiesStorage.register(new MapsSectionProcessingStrategy());
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException(e);
                        }
                    });

            bootstrap.add(mapsSectionItem);

            /* "endpoints" section */
            IBootstrapItem<String> endpointsSectionItem = new BootstrapItem("config_section:endpoints");

            endpointsSectionItem
                    .after("config_section:maps")
                    .before("configure")
                    .process(() -> {
                        try {
                            ISectionStrategiesStorage strategiesStorage =
                                    IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), ISectionStrategiesStorage.class.getCanonicalName()));

                            strategiesStorage.register(new EndpointsSectionProcessingStrategy());
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException(e);
                        }
                    });
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
