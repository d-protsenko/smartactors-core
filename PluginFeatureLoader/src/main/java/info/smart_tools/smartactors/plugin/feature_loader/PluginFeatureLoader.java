package info.smart_tools.smartactors.plugin.feature_loader;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.IBiAction;
import info.smart_tools.smartactors.base.strategy.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.feature_loader.FeatureLoader;
import info.smart_tools.smartactors.core.feature_loader.FeatureStatusImpl;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.ifeature_loader.GlobalFeatureLoader;
import info.smart_tools.smartactors.core.ifeature_loader.IFeatureLoader;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin_loader_visitor.IPluginLoaderVisitor;
import info.smart_tools.smartactors.feature_loading_system.plugin_creator.PluginCreator;
import info.smart_tools.smartactors.feature_loading_system.plugin_loader_from_jar.PluginLoader;
import info.smart_tools.smartactors.feature_loading_system.plugin_loader_visitor_empty_implementation.PluginLoaderVisitor;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;

/**
 *
 */
public class PluginFeatureLoader extends BootstrapPlugin {
    /**
     * The constructor.
     * @param bootstrap    the bootstrap
     */
    public PluginFeatureLoader(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        super(bootstrap);
    }

    /**
     * Item registering the feature loader and some related components.
     * @throws ResolutionException if cannot resolve the keys
     * @throws RegistrationException if cannot register a strategy
     * @throws InvalidArgumentException if some of called methods throws
     */
    @Item("feature_loader")
    @After({"IOC", "configuration_manager", "config_sections:done", "IFieldNamePlugin", "filesystem_facade", "ConfigurationObject"})
    public void registerFeatureLoader()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(Keys.getOrAdd(FeatureStatusImpl.class.getCanonicalName()), new CreateNewInstanceStrategy(args -> {
            try {
                return new FeatureStatusImpl((String) args[0], (IBiAction) args[1]);
            } catch (InvalidArgumentException e) {
                throw new RuntimeException(e);
            }
        }));
        IOC.register(Keys.getOrAdd("plugin creator"), new SingletonStrategy(new PluginCreator()));
        IOC.register(Keys.getOrAdd("plugin loader visitor"), new SingletonStrategy(new PluginLoaderVisitor<String>()));
        IOC.register(Keys.getOrAdd("plugin loader"), new CreateNewInstanceStrategy(args -> {
            try {
                return new PluginLoader(
                        (ClassLoader) args[0], (IAction<Class>) args[1], (IPluginLoaderVisitor) args[2]);
            } catch (InvalidArgumentException e) {
                throw new RuntimeException(e);
            }
        }));

        IFeatureLoader featureLoader = new FeatureLoader();
        GlobalFeatureLoader.set(featureLoader);
        IOC.register(Keys.getOrAdd("feature loader"), new SingletonStrategy(featureLoader));
    }
}
