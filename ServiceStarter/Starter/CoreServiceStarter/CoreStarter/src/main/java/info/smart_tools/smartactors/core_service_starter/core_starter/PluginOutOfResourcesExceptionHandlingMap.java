package info.smart_tools.smartactors.core_service_starter.core_starter;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.IConfigurationManager;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

/**
 * Plugin for adding map for handling OutOfResourcesException
 */
public class PluginOutOfResourcesExceptionHandlingMap implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    private String template =
        ("{" +
            "'objects': [" +
                "{" +
                    "'kind': 'raw'," +
                    "'dependency': 'RetryingToTakeResourceExceptionHandler'," +
                    "'name': 'retryingToTakeResourceExceptionHandler'" +
                "}" +
            "]," +
            "'maps': [" +
                "{" +
                    "'id': 'tryToTakeResourceMap'," +
                    "'steps': [" +
                        "{" +
                            "'target': 'retryingToTakeResourceExceptionHandler'" +
                        "}" +
                    "]," +
                    "'exceptional': []" +
                "}" +
            "]" +
        "}").replace('\'', '"');

    /**
     * The constructor.
     * @param bootstrap the bootstrap
     */
    public PluginOutOfResourcesExceptionHandlingMap(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("PluginOutOfResourcesExceptionHandlingMap");

            item
                    .after("config_sections:done")
                    .after("PluginRetryingToTakeResourceExceptionHandler")
                    .process(() -> {
                        try {
                            IConfigurationManager configurationManager =
                                    IOC.resolve(Keys.getOrAdd(IConfigurationManager.class.getCanonicalName()));

                            IObject configSection = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()), template);
                            configurationManager.applyConfig(configSection);
                        } catch (ResolutionException | InvalidArgumentException | ConfigurationProcessingException e) {
                            throw new ActionExecuteException(e);
                        }
                    });

            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
