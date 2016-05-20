package info.smart_tools.smartactors.core.server_with_plugin_loader;

import info.smart_tools.smartactors.core.bootstrap.Bootstrap;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
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
import info.smart_tools.smartactors.core.plugin_loader_visitor_empty_implementation.PluginLoaderVisitor;

import java.net.URL;

/**
 * Server with PluginLoader
 */
public class Server implements IServer {

    @Override
    public void initialize()
            throws ServerInitializeException {
        /*

            FeatureManager fm = new FeatureManager();
            ConfigReader cr = new ConfigReader(fm, PATH_TO_CONFIG);
            cr.read();
            DirectoryListener pluginLoader = new DirectoryListener(fm, PATH_TO_PLUGINS);
            pluginLoader.loadAllInDir();
            pluginLoader.listen();

             */

        try {
            IBootstrap bootstrap = new Bootstrap();
            IPluginCreator creator = new PluginCreator();
            IPluginLoaderVisitor<String> visitor = new PluginLoaderVisitor<>();
            ExpansibleURLClassLoader urlClassLoader = new ExpansibleURLClassLoader(new URL[]{}, ClassLoader.getSystemClassLoader());
            IPluginLoader<String> pluginLoader = new PluginLoader(
                    urlClassLoader,
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

            pluginLoader.loadPlugin("/home/sevenbits/Projects/libs/ScopeProvider.jar");
            pluginLoader.loadPlugin("/home/sevenbits/Projects/libs/PluginSubscribeScopeProviderOnScopeCreation.jar");
            bootstrap.start();
        } catch (Throwable e) {
            throw new ServerInitializeException("Server initialization failed.");
        }


    }

    @Override
    public void start() throws ServerExecutionException {

    }
}
