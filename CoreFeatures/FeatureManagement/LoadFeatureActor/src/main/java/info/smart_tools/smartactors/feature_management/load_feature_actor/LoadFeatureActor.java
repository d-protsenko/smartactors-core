package info.smart_tools.smartactors.feature_management.load_feature_actor;

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
import info.smart_tools.smartactors.feature_management.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.feature_management.load_feature_actor.exception.LoadFeatureException;
import info.smart_tools.smartactors.feature_management.load_feature_actor.wrapper.LoadFeatureWrapper;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

import java.io.File;
import java.lang.reflect.Modifier;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * Created by sevenbits on 12/7/16.
 */
public class LoadFeatureActor {

    private IPluginLoaderVisitor<String> pluginLoaderVisitor;
    private final IPluginCreator pluginCreator;
    private final IConfigurationManager configurationManager;

    public LoadFeatureActor()
            throws ResolutionException {
        this.pluginLoaderVisitor = IOC.resolve(Keys.getOrAdd("plugin loader visitor"));
        this.pluginCreator = IOC.resolve(Keys.getOrAdd("plugin creator"));
        configurationManager = IOC.resolve(Keys.getOrAdd(IConfigurationManager.class.getCanonicalName()));
    }

    public void load(final LoadFeatureWrapper wrapper)
            throws LoadFeatureException {
        IFeature feature;
        try {
            feature = wrapper.getFeature();
        } catch (ReadValueException e) {
            throw new LoadFeatureException("Feature should not be null.");
        }
        try {
            System.out.println("[INFO] Start loading feature - '" + feature.getName() + "'.");
            String pattern = ".jar";
            File file = Paths.get(((IPath) feature.getFeatureLocation()).getPath()).toFile();
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
            System.out.println("[OK] -------------- Feature - '" + feature.getName() + "' has been loaded successful.");
        } catch (Throwable e) {
            feature.setFailed(true);
            System.out.println("[FAILED] ---------- Feature '" + feature.getName() + "' loading has been broken with exception:");
            System.out.println(e);
        }
    }
}
