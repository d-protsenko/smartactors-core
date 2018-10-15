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
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

import java.util.ArrayList;
import java.util.List;

/**
 * Plugin for adding map for handling OutOfResourcesException
 */
public class PluginOutOfResourcesExceptionHandlingMap implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * The constructor.
     * @param bootstrap the bootstrap
     */
    public PluginOutOfResourcesExceptionHandlingMap(final IBootstrap<IBootstrapItem<String>> bootstrap) throws Exception {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("PluginOutOfResourcesExceptionHandlingMap");

            item
                    .after("config_sections:done")
                    .after("iobject")
                    .after("PluginRetryingToTakeResourceExceptionHandler")
                    .after("standard_object_creators")
                    .after("ConfigurationObject")
                    .after("messaging_identifiers")
                    .before("read_initial_config")
                    .after("object_configuration_canonization_strategies")
                    .process(() -> {
                        try {
                            IConfigurationManager configurationManager =
                                    IOC.resolve(Keys.getOrAdd(IConfigurationManager.class.getCanonicalName()));

                            IKey fieldNameKey = Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName");
                            IObject templateObj = IOC.resolve(Keys.getOrAdd("configuration object"));

                            IObject object = IOC.resolve(Keys.getOrAdd("configuration object"));
                            object.setValue(IOC.resolve(fieldNameKey, "kind"), "raw");
                            object.setValue(IOC.resolve(fieldNameKey, "dependency"), "RetryingToTakeResourceExceptionHandler");
                            object.setValue(IOC.resolve(fieldNameKey, "name"), "retryingToTakeResourceExceptionHandler");

                            List<IObject> objectsSection = new ArrayList<>(1);
                            objectsSection.add(object);
                            templateObj.setValue(IOC.resolve(fieldNameKey, "objects"), objectsSection);

                            IObject map = IOC.resolve(Keys.getOrAdd("configuration object"));
                            map.setValue(IOC.resolve(fieldNameKey, "id"), "tryToTakeResourceMap");

                            IObject step = IOC.resolve(Keys.getOrAdd("configuration object"));
                            step.setValue(IOC.resolve(fieldNameKey, "target"), "retryingToTakeResourceExceptionHandler");

                            List<IObject> steps = new ArrayList<>(1);
                            steps.add(step);
                            map.setValue(IOC.resolve(fieldNameKey, "steps"), steps);

                            map.setValue(IOC.resolve(fieldNameKey, "exceptional"), new ArrayList<IObject>(0));

                            List<IObject> mapsSection = new ArrayList<>(1);
                            mapsSection.add(map);
                            templateObj.setValue(IOC.resolve(fieldNameKey, "maps"), mapsSection);

                            configurationManager.applyConfig(templateObj);
                        } catch (ResolutionException | InvalidArgumentException | ChangeValueException | ConfigurationProcessingException e) {
                            throw new ActionExecuteException(e);
                        }
                    });

            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
