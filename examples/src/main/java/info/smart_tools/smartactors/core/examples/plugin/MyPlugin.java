package info.smart_tools.smartactors.core.examples.plugin;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;

/**
 *  Example of plugin.
 */
public class MyPlugin implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    public MyPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            // TODO: add dependency from IOC
            IBootstrapItem<String> item = new BootstrapItem("MyPlugin");
            item.process(() -> {
                System.out.println("MyPlugin initialized");
            });
            bootstrap.add(item);
        } catch (Exception e) {
            throw new PluginException("Could not load plugin", e);
        }
        System.out.println("MyPlugin loaded");
    }

}
