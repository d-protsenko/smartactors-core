package info.smart_tools.smartactors.server.server_for_intern;

import info.smart_tools.smartactors.core.bootstrap.Bootstrap;
import info.smart_tools.smartactors.core.feature_manager.FeatureManager;
import info.smart_tools.smartactors.core.filesystem_tracker.FilesystemTracker;
import info.smart_tools.smartactors.core.filesystem_tracker.ListenerTask;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.ifeature_manager.IFeature;
import info.smart_tools.smartactors.core.ifeature_manager.IFeatureManager;
import info.smart_tools.smartactors.core.ifilesystem_tracker.IFilesystemTracker;
import info.smart_tools.smartactors.base.interfaces.ipath.IPath;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin_creator.IPluginCreator;
import info.smart_tools.smartactors.core.iplugin_loader.IPluginLoader;
import info.smart_tools.smartactors.core.iplugin_loader_visitor.IPluginLoaderVisitor;
import info.smart_tools.smartactors.core.iserver.IServer;
import info.smart_tools.smartactors.core.iserver.exception.ServerExecutionException;
import info.smart_tools.smartactors.core.iserver.exception.ServerInitializeException;
import info.smart_tools.smartactors.base.path.Path;
import info.smart_tools.smartactors.core.plugin_creator.PluginCreator;
import info.smart_tools.smartactors.core.plugin_loader_from_jar.ExpansibleURLClassLoader;
import info.smart_tools.smartactors.core.plugin_loader_from_jar.PluginLoader;
import info.smart_tools.smartactors.core.plugin_loader_visitor_empty_implementation.PluginLoaderVisitor;

import java.io.File;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

/**
 *
 */
public class Server implements IServer {

    private static final String PLUGIN_FILE_NAME = "plugins.list";
    private static final String STARTER_FILE_NAME = "plugins.list";

    private List<String> corePlugins = new ArrayList<>();
    private List<String> usersPlugins = new ArrayList<>();
    private List<String> starterPlugins = new ArrayList<>();
    private ExpansibleURLClassLoader urlClassLoader = new ExpansibleURLClassLoader(new URL[]{}, ClassLoader.getSystemClassLoader());
    private IPath corePath = new Path("core");
    private IPath pluginsPath = new Path("plugins");
    private IPath starterPath = new Path("starter");
    private IFeatureManager featureManager;
    private BootstrapItems items = new BootstrapItems();

    public static void main(final String[] args) throws Exception {
        Server server = new Server();
        server.initialize();
        server.start();
    }

    @Override
    public void initialize()
            throws ServerInitializeException {
        try {
            this.featureManager = new FeatureManager();
            getCorePluginsList();
            getUserPluginsList();
            getStarterList();
            loadCorePlugins();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new ServerInitializeException(e);
        }
    }

    @Override
    public void start()
            throws ServerExecutionException {
        try {
            while (!Thread.interrupted()) {
                Thread.sleep(1L);
            }
        } catch (Throwable e) {
            throw new ServerExecutionException(e);
        }
    }

    private void loadCorePlugins()
            throws Exception {
        IBootstrap bootstrap = new Bootstrap();
        IPluginCreator creator = new PluginCreator();
        IPluginLoaderVisitor<String> visitor = new PluginLoaderVisitor<>();
        IPluginLoader<Collection<IPath>> pluginLoader = new PluginLoader(
                this.urlClassLoader,
                (t) -> {
                    try {
                        IPlugin plugin = creator.create(t, bootstrap);
                        plugin.load();
                    } catch (Exception e) {
                        throw new RuntimeException("Could not create instance of IPlugin");
                    }
                },
                visitor
        );
        IPath coreJarsDir = this.corePath;
        Files.createDirectories(FileSystems.getDefault().getPath(coreJarsDir.getPath()));

        IFilesystemTracker jarFilesTracker = new FilesystemTracker(
                (path) -> path.getPath().endsWith(".jar"),
                ListenerTask::new);

        jarFilesTracker.addErrorHandler((e) -> {
            System.out.println("Server initialization failed!");
            e.printStackTrace();
        });
        IFeature coreFeature = this.featureManager.newFeature("smartactors.core", jarFilesTracker);
        coreFeature.whenPresent(files -> {
            try {
                pluginLoader.loadPlugin(files);
                items.setItems(bootstrap.start());
                System.out.println("--------------------------------- Core plugins has been loaded ---------------------------------");
                loadUsersPlugins();
            } catch (Throwable e) {
                throw new RuntimeException("Plugin loading failed.", e);
            }
        });
        for (String jarName : this.corePlugins) {
            coreFeature.requireFile(jarName);
        }
        coreFeature.listen();
        jarFilesTracker.start(coreJarsDir);
        System.out.println("--------------------------------- Loading plugins from " + this.corePath + " listed in " + this.corePath + File.separator + PLUGIN_FILE_NAME + " ---------------------------------");
    }

    private void loadUsersPlugins()
            throws Exception {
        IBootstrap bootstrap = new Bootstrap(items.getItems());
        IPluginCreator creator = new PluginCreator();
        IPluginLoaderVisitor<String> visitor = new PluginLoaderVisitor<>();
        IPluginLoader<Collection<IPath>> pluginLoader = new PluginLoader(
                this.urlClassLoader,
                (t) -> {
                    try {
                        IPlugin plugin = creator.create(t, bootstrap);
                        plugin.load();
                    } catch (Exception e) {
                        throw new RuntimeException("Could not create instance of IPlugin");
                    }
                },
                visitor
        );
        IPath usersJarsDir = this.pluginsPath;
        Files.createDirectories(FileSystems.getDefault().getPath(usersJarsDir.getPath()));

        IFilesystemTracker jarFilesTracker = new FilesystemTracker(
                (path) -> path.getPath().endsWith(".jar"),
                ListenerTask::new);

        jarFilesTracker.addErrorHandler((e) -> {
            System.out.println("Server initialization failed!");
            e.printStackTrace();
        });
        IFeature pluginsFeature = featureManager.newFeature("smartactors.plugins", jarFilesTracker);
        pluginsFeature.whenPresent(files -> {
            try {
                pluginLoader.loadPlugin(files);
                items.setItems(bootstrap.start());
                System.out.println("--------------------------------- Users plugins has been loaded ---------------------------------");
                loadStarterPlugins();
            } catch (Throwable e) {
                throw new RuntimeException("Plugin loading failed.", e);
            }
        });
        for (String jarName : this.usersPlugins) {
            pluginsFeature.requireFile(jarName);
        }
        pluginsFeature.listen();
        jarFilesTracker.start(usersJarsDir);
        System.out.println("--------------------------------- Loading plugins from " + this.pluginsPath + " listed in " + this.pluginsPath + File.separator + PLUGIN_FILE_NAME + " ---------------------------------");
    }

    private void loadStarterPlugins()
            throws Exception {
        IBootstrap bootstrap = new Bootstrap(items.getItems());
        IPluginCreator creator = new PluginCreator();
        IPluginLoaderVisitor<String> visitor = new PluginLoaderVisitor<>();
        IPluginLoader<Collection<IPath>> pluginLoader = new PluginLoader(
                this.urlClassLoader,
                (t) -> {
                    try {
                        IPlugin plugin = creator.create(t, bootstrap);
                        plugin.load();
                    } catch (Exception e) {
                        throw new RuntimeException("Could not create instance of IPlugin");
                    }
                },
                visitor
        );
        IPath starterJarsDir = this.starterPath;
        Files.createDirectories(FileSystems.getDefault().getPath(starterJarsDir.getPath()));

        IFilesystemTracker jarFilesTracker = new FilesystemTracker(
                (path) -> path.getPath().endsWith(".jar"),
                ListenerTask::new);
        jarFilesTracker.addErrorHandler((e) -> {
            System.out.println("Server initialization failed!");
            e.printStackTrace();
        });
        IFeature starterFeature = featureManager.newFeature("smartactors.starter", jarFilesTracker);
        starterFeature.whenPresent(files -> {
            try {
                System.out.println("--------------------------------- Start system ---------------------------------");
                pluginLoader.loadPlugin(files);
                items.setItems(bootstrap.start());
            } catch (Throwable e) {
                throw new RuntimeException("Plugin loading failed.", e);
            }
        });
        for (String jarName : this.starterPlugins) {
            starterFeature.requireFile(jarName);
        }
        starterFeature.listen();
        jarFilesTracker.start(starterJarsDir);
        System.out.println("--------------------------------- Loading plugins from " + this.starterPath + " listed in " + this.starterPath + File.separator + PLUGIN_FILE_NAME + " ---------------------------------");
    }

    private void getCorePluginsList()
            throws Exception {
        Scanner s = new Scanner(new File(this.corePath + File.separator + PLUGIN_FILE_NAME));
        while (s.hasNext()) {
            this.corePlugins.add(s.next());
        }
    }

    private void getUserPluginsList()
            throws Exception {
        Scanner s = new Scanner(new File(this.pluginsPath + File.separator + PLUGIN_FILE_NAME));
        while (s.hasNext()) {
            this.usersPlugins.add(s.next());
        }
    }

    private void getStarterList()
            throws Exception {
        Scanner s = new Scanner(new File(this.starterPath + File.separator + STARTER_FILE_NAME));
        while (s.hasNext()) {
            this.starterPlugins.add(s.next());
        }
    }
}

class BootstrapItems {
    private List<IBootstrapItem<String>> items = new ArrayList<>();

    public List<IBootstrapItem<String>> getItems() {
        return items;
    }

    public void setItems(final List<IBootstrapItem<String>> items) {
        this.items = items;
    }
}
