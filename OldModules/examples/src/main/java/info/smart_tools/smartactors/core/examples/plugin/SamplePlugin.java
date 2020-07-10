package info.smart_tools.smartactors.core.examples.plugin;

import info.smart_tools.smartactors.base.strategy.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.examples.SampleClass;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.feature_loading_system.plugin_loader_from_jar.PluginLoader;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;

/**
 * Example of plugin.
 */
public class SamplePlugin implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;                 // to register our BootstrapItem

    /**
     * Creates the plugin.
     * This constructor is called by {@link PluginLoader},
     * so it must have one argument of type IBootstrap.
     * @param bootstrap bootstrap where this plugin puts {@link IBootstrapItem}.
     */
    public SamplePlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {   // this constructor signature is required
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("SamplePlugin");    // our item name
            item.after("IOC");                                                  // dependency, we need IOC
            item.process(() -> {
                try {
                    IKey key = Keys.getKeyByName("new SampleClass");
                    IOC.register(
                            key,
                            // it's the initialization action of our plugin
                            new CreateNewInstanceStrategy(
                                    (args) -> new SampleClass((String) args[0])
                            )
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                System.out.println("SamplePlugin initialized");
            });
            bootstrap.add(item);                                            // tell server about our intents
        } catch (Exception e) {
            throw new PluginException("Could not load SamplePlugin", e);
        }
        System.out.println("SamplePlugin loaded");
    }

}
