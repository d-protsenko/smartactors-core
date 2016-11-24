package info.smart_tools.smartactors.feature_manager_interfaces.feature_manager_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin_loader_visitor.IPluginLoaderVisitor;
import info.smart_tools.smartactors.feature_loading_system.plugin_creator.PluginCreator;
import info.smart_tools.smartactors.feature_loading_system.plugin_loader_from_jar.PluginLoader;
import info.smart_tools.smartactors.feature_loading_system.plugin_loader_visitor_empty_implementation.PluginLoaderVisitor;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.FeatureManager;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.FeatureManagerGlobal;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.FeatureState;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.IFeatureManager;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.LoadFeatureTask;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.UnzipFeatureTask;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

/**
 * Created by sevenbits on 11/21/16.
 */
public class FeatureManagerPlugin extends BootstrapPlugin {

    public FeatureManagerPlugin(IBootstrap bootstrap) {
        super(bootstrap);
    }

    @Item("feature_manager")
    @After({"IOC", "configuration_manager", "config_sections:done", "IFieldNamePlugin", "ConfigurationObject"})
    public void registerStrategies()
            throws ResolutionException, RegistrationException, InvalidArgumentException {

//        IQueue queue = IOC.resolve(Keys.getOrAdd("task_queue"));

        IFeatureManager featureManager = new FeatureManager(/*queue*/);
        FeatureManagerGlobal.set(featureManager);

        IOC.register(
                Keys.getOrAdd("feature-manager"),
                new SingletonStrategy(featureManager)
        );

        IOC.register(
                Keys.getOrAdd("load-feature"),
                new ApplyFunctionToArgumentsStrategy(args -> {
                    try {
                        return new LoadFeatureTask((IFeatureManager) args[0], (IFeature) args[1]);
                    } catch (ResolutionException e) {
                        throw new RuntimeException(e);
                    }
                })
        );

        IOC.register(
                Keys.getOrAdd("unzip-feature"),
                new ApplyFunctionToArgumentsStrategy(args -> {
                    try {
                        return new UnzipFeatureTask((IFeatureManager) args[0], (IFeature) args[1]);
                    } catch (ResolutionException e) {
                        throw new RuntimeException(e);
                    }
                })
        );

        IOC.register(
                Keys.getOrAdd("feature-state:initial-state"),
                new ApplyFunctionToArgumentsStrategy(args -> {
                    return new FeatureState(new String[]{"unzip-feature", "load-feature"});
            })
        );

        IOC.register(Keys.getOrAdd("plugin creator"), new SingletonStrategy(new PluginCreator()));
        IOC.register(Keys.getOrAdd("plugin loader visitor"), new SingletonStrategy(new PluginLoaderVisitor<String>()));
        IOC.register(Keys.getOrAdd("plugin loader"), new ApplyFunctionToArgumentsStrategy(args -> {
            try {
                return new PluginLoader(
                        getClass().getClassLoader(), (IAction<Class>) args[0], (IPluginLoaderVisitor) args[1]);
            } catch (InvalidArgumentException e) {
                throw new RuntimeException(e);
            }
        }));
    }
}
