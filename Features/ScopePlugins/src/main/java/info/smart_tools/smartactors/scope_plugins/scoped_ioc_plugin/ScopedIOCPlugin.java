package info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin;

import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecutionException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.scope.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;

/**
 *
 */
public class ScopedIOCPlugin implements IPlugin {
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public ScopedIOCPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            /* "subscribe_ioc_for_scope_creation" - create new strategy container in new scopes */
            IBootstrapItem<String> subscribeItem = IOC.resolve(
                    Keys.getKeyByName("bootstrap item"),
                    "subscribe_ioc_for_scope_creation"
            );

            subscribeItem
                    .before("create_system_scope")
                    .after("scope_provider_container")
                    .process(() -> {
                        try {
                            ScopeProvider.subscribeOnCreationNewScope(scope -> {
                                try {
                                    scope.setValue(IOC.getIocKey(), new StrategyContainer());
                                } catch (Exception e) {
                                    throw new Error(e);
                                }
                            });
                        } catch (ScopeProviderException e) {
                            throw new ActionExecutionException("ScopedIOC plugin can't load.", e);
                        }
                    });

            bootstrap.add(subscribeItem);

            /* "ioc_container" - create and set container */
            IBootstrapItem<String> containerItem = IOC.resolve(
                    Keys.getKeyByName("bootstrap item"),
                    "ioc_container"
            );

            containerItem
                    .after("create_system_scope")
                    .process(() -> { /* container is set by default so do nothing */ });

            bootstrap.add(containerItem);

            /* "IOC" - barrier after which the IOC should be ready to use */
            IBootstrapItem<String> iocItem = IOC.resolve(
                    Keys.getKeyByName("bootstrap item"),
                    "IOC"
            );

            iocItem
                    .after("ioc_container")
                    .process(() -> { });

            bootstrap.add(iocItem);
        } catch (ResolutionException e) {
            throw new PluginException(e);
        }
    }
}
