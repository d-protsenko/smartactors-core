package info.smart_tools.smartactors.core.examples;

import info.smart_tools.smartactors.core.bootstrap.Bootstrap;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.iplugin_creator.IPluginCreator;
import info.smart_tools.smartactors.core.iplugin_loader.IPluginLoader;
import info.smart_tools.smartactors.core.iplugin_loader_visitor.IPluginLoaderVisitor;
import info.smart_tools.smartactors.core.iserver.IServer;
import info.smart_tools.smartactors.core.iserver.exception.ServerExecutionException;
import info.smart_tools.smartactors.core.iserver.exception.ServerInitializeException;
import info.smart_tools.smartactors.core.plugin_creator.PluginCreator;
import info.smart_tools.smartactors.core.plugin_loader_from_jar.ExpansibleURLClassLoader;
import info.smart_tools.smartactors.core.plugin_loader_from_jar.PluginLoader;

import java.net.URL;

/**
 *  These are examples of using plugins.
 */
public class PluginExample {

    /**
     *  Sample server implementation.
     */
    static class MyServer implements IServer {

        @Override
        public void initialize() throws ServerInitializeException {
            try {
                IBootstrap bootstrap = new Bootstrap();         // initializes the server in correct order
                IPluginCreator creator = new PluginCreator();   // instantiates the plugin correctly
                IPluginLoaderVisitor<String> visitor = new MyPluginVisitor();   // checks the plugin loadings

                ClassLoader urlClassLoader =
                        new ExpansibleURLClassLoader(new URL[]{}, ClassLoader.getSystemClassLoader());  // loads plugins' classes

                IPluginLoader<String> pluginLoader = new PluginLoader(      // loads plugins to classloader
                        urlClassLoader,
                        (t) -> {
                            try {
                                IPlugin plugin = creator.create(t, bootstrap);  // creates the plugin instance
                                plugin.load();                                  // loads the plugin
                            } catch (Exception e) {
                                throw new RuntimeException("Could not create instance of IPlugin");
                            }
                        },
                        visitor
                );

                // TODO: get jars from Maven repository
                pluginLoader.loadPlugin("libs/MyPlugin.jar");       // loads plugins
                bootstrap.start();                                  // starts initialization

            } catch (Throwable e) {
                throw new ServerInitializeException("Server initialization failed.", e);
            }
        }

        @Override
        public void start() throws ServerExecutionException {
        }
    }

    static class MyPlugin implements IPlugin {

        private final IBootstrap<IBootstrapItem<String>> bootstrap;

        public MyPlugin(IBootstrap<IBootstrapItem<String>> bootstrap) {
            this.bootstrap = bootstrap;
        }

        @Override
        public void load() throws PluginException {
            try {
                // TODO: add dependency from IOC
                IBootstrapItem<String> item = new BootstrapItem("MyPlugin");
                item.process(()-> {
                    System.out.println("MyPlugin initialized");
                });
                bootstrap.add(item);
            } catch (Exception e) {
                throw new PluginException("Could not load plugin", e);
            }
        }

    }

    static class MyPluginVisitor implements IPluginLoaderVisitor<String> {

        @Override
        public void pluginLoadingFail(String value, Throwable e) {
            System.out.println(value + " plugin load failed");
            System.out.println(String.valueOf(e));
        }

        @Override
        public void packageLoadingFail(String value, Throwable e) {
            System.out.println(value + " package load failed");
            System.out.println(String.valueOf(e));
        }

        @Override
        public void pluginLoadingSuccess(String value) {
            System.out.println(value + " plugin loaded successfully");
        }

        @Override
        public void packageLoadingSuccess(String value) {
            System.out.println(value + " package loaded successfully");
        }
    }

}
