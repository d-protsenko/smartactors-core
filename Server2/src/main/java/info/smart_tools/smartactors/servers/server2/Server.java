package info.smart_tools.smartactors.servers.server2;

//import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
//import info.smart_tools.smartactors.base.interfaces.iaction.IPoorAction;
//import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
//import info.smart_tools.smartactors.base.interfaces.ipath.IPath;
//import info.smart_tools.smartactors.base.path.Path;
//import info.smart_tools.smartactors.feature_loading_system.bootstrap.Bootstrap;
//import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
//import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.exception.ProcessExecutionException;
//import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.exception.RevertProcessExecutionException;
//import info.smart_tools.smartactors.feature_loader.interfaces.ifeature_loader.GlobalFeatureLoader;
//import info.smart_tools.smartactors.feature_loader.interfaces.ifeature_loader.exceptions.FeatureLoadException;
//import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
//import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
//import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin_creator.IPluginCreator;
//import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin_creator.exception.PluginCreationException;
//import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin_loader.IPluginLoader;
//import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin_loader.exception.PluginLoaderException;
//import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin_loader_visitor.IPluginLoaderVisitor;
//import info.smart_tools.smartactors.server_developing_tools.interfaces.iserver.IServer;
//import info.smart_tools.smartactors.server_developing_tools.interfaces.iserver.exception.ServerExecutionException;
//import info.smart_tools.smartactors.server_developing_tools.interfaces.iserver.exception.ServerInitializeException;
//import info.smart_tools.smartactors.feature_loading_system.plugin_creator.PluginCreator;
//import info.smart_tools.smartactors.feature_loading_system.plugin_loader_from_jar.ExpansibleURLClassLoader;
//import info.smart_tools.smartactors.feature_loading_system.plugin_loader_from_jar.PluginLoader;
//import info.smart_tools.smartactors.feature_loading_system.plugin_loader_visitor_empty_implementation.PluginLoaderVisitor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.text.MessageFormat;
import java.util.LinkedList;

/**
 *
 */
public class Server implements IServer {
    private ExpansibleURLClassLoader classLoader = new ExpansibleURLClassLoader(new URL[]{});
    private IPluginLoaderVisitor<String> pluginLoaderVisitor = new PluginLoaderVisitor<>();
    private IPluginCreator pluginCreator = new PluginCreator();

    /**
     * Runs the server.
     *
     * @param args    command line arguments
     * @throws Exception if any error occurs
     */
    public static void main(final String[] args)
            throws Exception {
        IServer server = new ServerWithFeatures();
        server.initialize();
        server.start();
    }

    @Override
    public void initialize() throws ServerInitializeException {

    }

    @Override
    public void start() throws ServerExecutionException {
        loadStage1();
        loadStage2();
    }

    private void loadStage1()
            throws ServerExecutionException {
        try {
            File coreDir = new File("core");

            List<IPath> jars = listJarsIn(coreDir);

            loadPluginsFrom(jars);

            System.out.println("Stage 1 (server core) completed successful.");
        } catch (IOException | InvalidArgumentException | PluginLoaderException | ProcessExecutionException e) {
            throw new ServerExecutionException(e);
        }
    }

    private void loadStage2()
            throws ServerExecutionException {
        IPoorAction loadUserFeatures = () -> loadFeatureGroup(new Path("features"), () -> { }, "2.2 (user features)");
        loadFeatureGroup(new Path("corefeatures"), loadUserFeatures, "2.1 (core features)");
    }

    private void loadFeatureGroup(final IPath groupPath, final IPoorAction onSuccess, final String stageTitle) {
        try {
            GlobalFeatureLoader.get().loadGroup(groupPath).whenDone(err -> {
                if (err == null) {
                    System.out.println(MessageFormat.format("Stage {0} completed successful.", stageTitle));
                    onSuccess.execute();
                    return;
                }

                System.err.println(MessageFormat.format("Stage {0} failed with exception:", stageTitle));
                err.printStackTrace(System.err);
                System.exit(1);
            });
        } catch (FeatureLoadException | ActionExecuteException e) {
            System.err.println(MessageFormat.format("Could not start stage {0} because of exception:", stageTitle));
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private List<IPath> listJarsIn(final File directory)
            throws IOException {
        if (!directory.isDirectory()) {
            throw new IOException(MessageFormat.format("File ''{0}'' is not a directory.", directory.getAbsolutePath()));
        }

        File[] files = directory.listFiles((dir, name) -> name.endsWith(".jar"));

        List<IPath> paths = new LinkedList<>();

        for (File file : files) {
            if (file.isFile()) {
                paths.add(new Path(file));
            }
        }

        return paths;
    }

    private void loadPluginsFrom(final List<IPath> jars)
            throws InvalidArgumentException, PluginLoaderException, ProcessExecutionException {
        IBootstrap bootstrap = new Bootstrap();
        IPluginLoader<Collection<IPath>> pluginLoader = new PluginLoader(
                classLoader,
                clz -> {
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
                },
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
    }
}
