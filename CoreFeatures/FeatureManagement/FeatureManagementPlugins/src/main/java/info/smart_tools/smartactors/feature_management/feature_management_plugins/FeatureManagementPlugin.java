package info.smart_tools.smartactors.feature_management.feature_management_plugins;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin_loader_visitor.IPluginLoaderVisitor;
import info.smart_tools.smartactors.feature_loading_system.plugin_creator.PluginCreator;
import info.smart_tools.smartactors.feature_loading_system.plugin_loader_from_jar.PluginLoader;
import info.smart_tools.smartactors.feature_loading_system.plugin_loader_visitor_empty_implementation.PluginLoaderVisitor;
import info.smart_tools.smartactors.feature_management.after_features_callback_storage.AfterFeaturesCallbackStorage;
import info.smart_tools.smartactors.feature_management.all_in_direcory_feature_tracker.AllInDirectoryFeatureTracker;
import info.smart_tools.smartactors.feature_management.download_feature_actor.DownloadFeatureActor;
import info.smart_tools.smartactors.feature_management.feature_manager_actor.FeatureManagerActor;
import info.smart_tools.smartactors.feature_management.load_feature_actor.LoadFeatureActor;
import info.smart_tools.smartactors.feature_management.unzip_feature_actor.UnzipFeatureActor;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.feature_management.feature_creator_actor.FeaturesCreatorActor;
import info.smart_tools.smartactors.feature_management.directory_watcher_actor.RuntimeDirectoryFeatureTracker;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;

import java.util.ArrayList;

/**
 * Plugin registers needed strategies to the IOC
 */
public class FeatureManagementPlugin extends BootstrapPlugin {

    /**
     * Creates instance of {@link FeatureManagementPlugin} by specific arguments
     * @param bootstrap the instance of {@link IBootstrap}
     */
    public FeatureManagementPlugin(final IBootstrap bootstrap) {
        super(bootstrap);
    }

    /**
     * Registers dependencies to the IOC
     * @throws ResolutionException if any errors occurred on IOC resolution
     * @throws RegistrationException if any errors occurred on registration dependency to the IOC
     * @throws InvalidArgumentException if any errors occurred on creation objects
     * @throws ChainNotFoundException if any errors occurred on looking for chain
     * @throws ChangeValueException if any errors occurred on writing to the {@link IObject}
     */
    @Item("feature_management")
    @After({"IOC", "configuration_manager", "config_sections:done", "IFieldNamePlugin", "ConfigurationObject", "config_section:onFeatureLoading"})
    @Before("read_initial_config")
    public void register()
            throws ResolutionException, RegistrationException, InvalidArgumentException, ChainNotFoundException, ChangeValueException {

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

        IOC.register(Keys.getOrAdd("feature-repositories"), new SingletonStrategy(
                new ArrayList<IObject>()
        ));

        IOC.register(Keys.getOrAdd("FeatureManager"), new ApplyFunctionToArgumentsStrategy(
                (args) -> {
                    try {
                        return new FeatureManagerActor();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
        IOC.register(
                Keys.getOrAdd("feature group load completion task queue"),
                new ApplyFunctionToArgumentsStrategy(
                        args -> AfterFeaturesCallbackStorage.getLocaleCallbackQueue()
                )
        );
        IOC.register(Keys.getOrAdd("DownloadFeatureActor"), new ApplyFunctionToArgumentsStrategy(
                (args) -> {
                    try {
                        return new DownloadFeatureActor();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
        IOC.register(Keys.getOrAdd("UnzipFeatureActor"), new ApplyFunctionToArgumentsStrategy(
                (args) -> {
                    try {
                        return new UnzipFeatureActor();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
        IOC.register(Keys.getOrAdd("LoadFeatureActor"), new ApplyFunctionToArgumentsStrategy(
                (args) -> {
                    try {
                        return new LoadFeatureActor();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
        IOC.register(Keys.getOrAdd("AllInDirectoryFeatureTracker"), new ApplyFunctionToArgumentsStrategy(
                (args) -> {
                    try {
                        return new AllInDirectoryFeatureTracker();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
        IOC.register(Keys.getOrAdd("DirectoryWatcherActor"), new ApplyFunctionToArgumentsStrategy(
                (args) -> {
                    try {
                        return new RuntimeDirectoryFeatureTracker();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
        );
        IOC.register(Keys.getOrAdd("FeatureCreatorActor"), new ApplyFunctionToArgumentsStrategy(
                (args) -> {
                    try {
                        return new FeaturesCreatorActor();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
        );
    }
}
