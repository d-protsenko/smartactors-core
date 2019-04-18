package info.smart_tools.smartactors.plugin.load_scope_povider;

import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;

/**
 * Plugin.
 * Implements {@link IPlugin}
 * Load ScopeProvider.
 */
public class LoadScopeProvider implements IPlugin {

    /** Local storage for instance of {@link IBootstrap}*/
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor with single argument
     * @param bootstrap instance of {@link IBootstrap}
     * @throws InvalidArgumentException if any errors occurred
     */
    public LoadScopeProvider(final IBootstrap<IBootstrapItem<String>> bootstrap)
            throws InvalidArgumentException {
        if (null == bootstrap) {
            throw new InvalidArgumentException("Incoming argument should not be null.");
        }
        this.bootstrap = bootstrap;
    }

    @Override
    public void load()
            throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("LoadScopeProvider");
            item
                    .process(
                            () -> {
                            }
                    );
            this.bootstrap.add(item);
        } catch (Throwable e) {
            throw new PluginException("Load plugin execution has been failed.", e);
        }
    }
}
