package info.smart_tools.smartactors.core.feature_loader;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.ifeature_loader.GlobalFeatureLoader;
import info.smart_tools.smartactors.core.ifeature_loader.IFeatureLoader;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.iplugin_loader_visitor.IPluginLoaderVisitor;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.plugin_creator.PluginCreator;
import info.smart_tools.smartactors.core.plugin_loader_from_jar.PluginLoader;
import info.smart_tools.smartactors.core.plugin_loader_visitor_empty_implementation.PluginLoaderVisitor;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;

/**
 *
 */
public class PluginFeatureLoader implements IPlugin {
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * The constructor
     *
     * @param bootstrap    the bootstrap
     */
    public PluginFeatureLoader(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> loaderItem = new BootstrapItem("feature_loader");

            loaderItem
                    .after("IOC")
                    .after("configuration_manager")
                    .after("config_sections:done")
                    .after("IFieldNamePlugin")
                    .after("ConfigurationObject")
                    .process(() -> {
                        try {

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
                        } catch (ResolutionException | RegistrationException | InvalidArgumentException e) {
                            throw new ActionExecuteException(e);
                        }
                    });

            bootstrap.add(loaderItem);

        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
