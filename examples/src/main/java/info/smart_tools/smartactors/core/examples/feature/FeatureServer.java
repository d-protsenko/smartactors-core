package info.smart_tools.smartactors.core.examples.feature;

import info.smart_tools.smartactors.core.bootstrap.Bootstrap;
import info.smart_tools.smartactors.core.examples.plugin.MyPluginVisitor;
import info.smart_tools.smartactors.core.feature_manager.FeatureManager;
import info.smart_tools.smartactors.core.filesystem_tracker.FilesystemTracker;
import info.smart_tools.smartactors.core.filesystem_tracker.ListenerTask;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ifeature_manager.IFeature;
import info.smart_tools.smartactors.core.ifeature_manager.IFeatureManager;
import info.smart_tools.smartactors.core.ifilesystem_tracker.IFilesystemTracker;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin_creator.IPluginCreator;
import info.smart_tools.smartactors.core.iplugin_loader.IPluginLoader;
import info.smart_tools.smartactors.core.iplugin_loader_visitor.IPluginLoaderVisitor;
import info.smart_tools.smartactors.core.iserver.IServer;
import info.smart_tools.smartactors.core.iserver.exception.ServerExecutionException;
import info.smart_tools.smartactors.core.iserver.exception.ServerInitializeException;
import info.smart_tools.smartactors.core.plugin_creator.PluginCreator;
import info.smart_tools.smartactors.core.plugin_loader_from_jar.ExpansibleURLClassLoader;
import info.smart_tools.smartactors.core.plugin_loader_from_jar.PluginLoader;

import java.io.File;
import java.net.URL;
import java.util.Collection;

/**
 *  Sample server implementation.
 */
public class FeatureServer implements IServer {

    private final Object initWaiter;
    private volatile boolean initialized = false;

    public FeatureServer(Object initWaiter) {
        this.initWaiter = initWaiter;
    }

    @Override
    public void initialize() throws ServerInitializeException {
        try {
            IBootstrap bootstrap = new Bootstrap();         // initializes the server in correct order
            IPluginCreator creator = new PluginCreator();   // instantiates the plugin correctly
            IPluginLoaderVisitor<String> visitor = new MyPluginVisitor();   // checks the plugin loadings

            ClassLoader urlClassLoader =
                new ExpansibleURLClassLoader(new URL[]{}, ClassLoader.getSystemClassLoader());  // loads plugins' classes

            IPluginLoader<Collection<File>> pluginLoader = new PluginLoader(
                    // loads plugins to classloader
                    urlClassLoader,
                    (t) -> {
                        try {
                            IPlugin plugin = creator.create(t, bootstrap);  // creates the plugin instance
                            plugin.load();                                  // loads the plugin
                        } catch (Exception e) {
                            throw new RuntimeException("Could not create instance of IPlugin", e);
                        }
                    },
                    visitor
            );

            // calls handlers when a new file appeared in the tracking directory
            IFilesystemTracker jarFilesTracker = new FilesystemTracker(
                    (dir, name) -> name.endsWith(".jar"),
                    ListenerTask::new);
            // is called when a tracking error appeared
            jarFilesTracker.addErrorHandler((e) -> {
                System.out.println("Failed to track files" + e);
                e.printStackTrace();
                notifyInitWaiter();
                throw new RuntimeException(e);
            });
            // is called when a new file is created in the directory
            jarFilesTracker.addFileHandler((file) -> {
                System.out.println("Found file: " + file);
            });

            // takes care on feature initialization while new jars appeared
            IFeatureManager featureManager = new FeatureManager(jarFilesTracker);

            // a new feature for the set of jars
            IFeature feature = featureManager.newFeature("example.feature");
            // what files to wait
            String[] jars = {
                "Plugin1.jar",
                "Plugin2.jar"
            };
            for (String jarName : jars) {
                feature.requireFile(jarName);
            }
            // what to do when the feature found all necessary files
            feature.whenPresent(files -> {
                try {
                    pluginLoader.loadPlugin(files);
                    bootstrap.start();
                    notifyInitWaiter();
                } catch (Throwable e) {
                    throw new RuntimeException("Plugin loading failed.", e);
                }
            });
            // subscribe to files creation
            feature.listen();

            // directory where jars are located
            File jarsDir = new File("target/libs");
            // start monitoring in daemon thread
            jarFilesTracker.start(jarsDir);

        } catch (Throwable e) {
            throw new ServerInitializeException(e);
        }
    }

    private void notifyInitWaiter() {
        synchronized (initWaiter) {
            initialized = true;
            initWaiter.notifyAll();
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void start() throws ServerExecutionException {
    }

}
