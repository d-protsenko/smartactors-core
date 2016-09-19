package info.smart_tools.smartactors.plugin.global_constants;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.constants_section_strategy.ConstantsSectionStrategy;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iconfiguration_manager.IConfigurationManager;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;

/**
 * Plugin that creates and registers global constants object and registers a strategy processing "const" section  of configuration.
 */
public class PluginGlobalConstants implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     *
     * @param bootstrap the bootstrap
     */
    public PluginGlobalConstants(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            /* "constants_object" - create global constants object */
            IBootstrapItem<String> constantsObjectItem = new BootstrapItem("constants_object");

            constantsObjectItem.after("IOC");
            constantsObjectItem.after("iobject");
            constantsObjectItem.process(() -> {
                try {
                    IObject obj = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
                    IOC.register(Keys.getOrAdd("global constants"), new SingletonStrategy(obj));
                } catch (ResolutionException | InvalidArgumentException | RegistrationException e) {
                    throw new ActionExecuteException(e);
                }
            });

            bootstrap.add(constantsObjectItem);

            /* "config_section:const" - register strategy processing "const" section of config */
            IBootstrapItem<String> constantsSectionItem = new BootstrapItem("config_section:const");

            constantsSectionItem.after("constants_object");
            constantsSectionItem.after("config_sections:start");
            constantsSectionItem.before("config_sections:done");
            constantsSectionItem.process(() -> {
                try {
                    IConfigurationManager configurationManager = IOC.resolve(Keys.getOrAdd(IConfigurationManager.class.getCanonicalName()));
                    configurationManager.addSectionStrategy(new ConstantsSectionStrategy());
                } catch (ResolutionException | InvalidArgumentException e) {
                    throw new ActionExecuteException(e);
                }
            });

            bootstrap.add(constantsSectionItem);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
