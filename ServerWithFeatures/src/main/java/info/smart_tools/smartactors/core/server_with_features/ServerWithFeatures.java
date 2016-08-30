package info.smart_tools.smartactors.core.server_with_features;

import info.smart_tools.smartactors.core.bootstrap.Bootstrap;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap.exception.ProcessExecutionException;
import info.smart_tools.smartactors.core.ibootstrap.exception.RevertProcessExecutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ipath.IPath;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.iplugin_creator.IPluginCreator;
import info.smart_tools.smartactors.core.iplugin_creator.exception.PluginCreationException;
import info.smart_tools.smartactors.core.iplugin_loader.IPluginLoader;
import info.smart_tools.smartactors.core.iplugin_loader.exception.PluginLoaderException;
import info.smart_tools.smartactors.core.iplugin_loader_visitor.IPluginLoaderVisitor;
import info.smart_tools.smartactors.core.iserver.IServer;
import info.smart_tools.smartactors.core.iserver.exception.ServerExecutionException;
import info.smart_tools.smartactors.core.iserver.exception.ServerInitializeException;
import info.smart_tools.smartactors.core.path.Path;
import info.smart_tools.smartactors.core.plugin_creator.PluginCreator;
import info.smart_tools.smartactors.core.plugin_loader_from_jar.ExpansibleURLClassLoader;
import info.smart_tools.smartactors.core.plugin_loader_from_jar.PluginLoader;
import info.smart_tools.smartactors.core.plugin_loader_visitor_empty_implementation.PluginLoaderVisitor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class ServerWithFeatures implements IServer {
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
    }

    private void loadStage1()
            throws ServerExecutionException {
        try {
            File coreDir = new File("core");

            List<IPath> jars = listJarsIn(coreDir);

            loadPluginsFrom(jars);
        } catch (IOException | InvalidArgumentException | PluginLoaderException | ProcessExecutionException e) {
            throw new ServerExecutionException(e);
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
