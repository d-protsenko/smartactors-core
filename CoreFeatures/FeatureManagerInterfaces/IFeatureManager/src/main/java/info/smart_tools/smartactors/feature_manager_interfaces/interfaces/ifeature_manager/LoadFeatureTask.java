package info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.interfaces.ipath.IPath;
import info.smart_tools.smartactors.base.path.Path;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.IConfigurationManager;
import info.smart_tools.smartactors.feature_loading_system.bootstrap.Bootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.exception.ProcessExecutionException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.exception.RevertProcessExecutionException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin_creator.IPluginCreator;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin_creator.exception.PluginCreationException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin_loader.IPluginLoader;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin_loader_visitor.IPluginLoaderVisitor;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature.IFeatureState;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.exception.FeatureManagementException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;

import java.io.File;
import java.lang.reflect.Modifier;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * Created by sevenbits on 11/15/16.
 */
public class LoadFeatureTask implements ITask {

    private IFeatureManager featureManager;
    private IFeature feature;
    private IPluginLoaderVisitor<String> pluginLoaderVisitor;
    private final IPluginCreator pluginCreator;

    private final IConfigurationManager configurationManager;

    public LoadFeatureTask(final IFeatureManager manager, final IFeature feature)
            throws ResolutionException {

        this.featureManager = manager;
        this.feature = feature;
        this.pluginLoaderVisitor = IOC.resolve(Keys.getOrAdd("plugin loader visitor"));
        this.pluginCreator = IOC.resolve(Keys.getOrAdd("plugin creator"));
        configurationManager = IOC.resolve(Keys.getOrAdd(IConfigurationManager.class.getCanonicalName()));
    }

    public void execute()
            throws TaskExecutionException {
        try {
            System.out.println("[INFO] Start loading feature - '" + feature.getName() + "'.");
            String pattern = ".jar";
            File file = Paths.get(((IPath) this.feature.getFeatureLocation()).getPath()).toFile();
            Collection<IPath> jars = new ArrayList<>();
            Stream.of(file.listFiles((item, string) ->  string.endsWith(pattern))).map(Path::new).forEach(jars::add);

            IBootstrap<IBootstrapItem<String>> bootstrap = new Bootstrap();
            IAction<Class> classHandler = clz -> {
                try {
                    if (Modifier.isAbstract(clz.getModifiers())) {
                        // Ignore abstract classes.
                        return;
                    }

                    IPlugin plugin = pluginCreator.create(clz, bootstrap);
                    plugin.load();
                } catch (PluginCreationException | PluginException e) {
                    throw new ActionExecuteException(e);
                }
            };

            IPluginLoader<Collection<IPath>> pluginLoader = IOC.resolve(
                    Keys.getOrAdd("plugin loader"),
                    classHandler,
                    pluginLoaderVisitor);
            pluginLoader.loadPlugin(jars);

            try {
                bootstrap.start();
            } catch (ProcessExecutionException e) {
                try {
                    bootstrap.revert();
                } catch (RevertProcessExecutionException ee) {
                    e.addSuppressed(ee);
                }

                throw e;
            }

            File configFile = file.listFiles((item, string) ->  string.equals("config.json"))[0];
            String configString = new Scanner(configFile).useDelimiter("\\Z").next();
            configurationManager.applyConfig(
                    IOC.resolve(Keys.getOrAdd("configuration object"), configString)
            );
            ((IFeatureState) this.feature.getStatus()).setLastSuccess(true);
            System.out.println("[OK] -------------- Feature - '" + feature.getName() + "' has been loaded successful.");
        } catch (Throwable e) {
            ((IFeatureState) this.feature.getStatus()).setLastSuccess(false);
            System.out.println("[FAILED] ---------- Feature '" + feature.getName() + "' loading has been broken with exception:");
            System.out.println(e);
        }

        try {
            this.featureManager.onCompleteFeatureOperation(this.feature);
        } catch (FeatureManagementException e) {
            System.out.println(e);
        }
    }
}
