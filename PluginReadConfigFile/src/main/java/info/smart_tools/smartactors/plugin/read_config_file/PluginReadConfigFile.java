package info.smart_tools.smartactors.plugin.read_config_file;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iconfiguration_manager.IConfigurationManager;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.invalid_state_exception.InvalidStateException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Reads configuration object from file. Name of the configuration file should be stored in environment variable "SM_CONFIG_FILE". By
 * default file named "configuration.json" from working directory is used.
 */
public class PluginReadConfigFile implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public PluginReadConfigFile(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> readConfigItem = new BootstrapItem("read_config");

            readConfigItem
                    .after("configuration_manager")
                    .after("iobject")
                    .after("ConfigurationObject")
                    .before("starter")
                    .process(() -> {
                        try {
                            String fileName = System.getenv().getOrDefault("SM_CONFIG_FILE", "configuration.json");
                            byte[] rawConfig = Files.readAllBytes(Paths.get(fileName));
                            String configString = new String(rawConfig);

                            IConfigurationManager configurationManager = IOC.resolve(
                                    Keys.getOrAdd(IConfigurationManager.class.getCanonicalName()));

//                            IObject config = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()), configString);
                            IObject cObject = IOC.resolve(Keys.getOrAdd("configuration object"), configString);

                            configurationManager.setInitialConfig(cObject);
                        } catch (FileNotFoundException e) {
                            throw new ActionExecuteException("ReadConfigFile plugin can't load: configuration file not found.", e);
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException("ReadConfigFile plugin can't load: can't get ReadConfigFile key", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecuteException("ReadConfigFile plugin can't load: can't create strategy", e);
                        } catch (InvalidStateException e) {
                            throw new ActionExecuteException("ReadConfigFile plugin can't load: configuration is already parsed", e);
                        } catch (IOException e) {
                            throw new ActionExecuteException("ReadConfigFile plugin can't load: can't read configuration file", e);
                        }
                    });

            bootstrap.add(readConfigItem);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
