package info.smart_tools.smartactors.global_constants_service_starter.global_constants_starter;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.IConfigurationManager;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

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

//            constantsObjectItem.after("IOC");
//            constantsObjectItem.before("starter");
//            constantsObjectItem.after("iobject");
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
//            constantsSectionItem.after("config_sections:start");
//            constantsSectionItem.before("config_sections:done");
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
