package info.smart_tools.smartactors.plugin.scoped_ioc;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;

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
            IBootstrapItem<String> subscribeItem = new BootstrapItem("subscribe_ioc_for_scope_creation");

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
                            throw new ActionExecuteException(e);
                        }
                    });

            bootstrap.add(subscribeItem);

            /* "ioc_container" - create and set container */
            IBootstrapItem<String> containerItem = new BootstrapItem("ioc_container");

            containerItem
                    .after("create_system_scope")
                    .process(() -> { /* container is set by default so do nothing */ });

            bootstrap.add(containerItem);

            /* "ioc" - barrier after which the IOC should be ready to use */
            IBootstrapItem<String> iocItem = new BootstrapItem("IOC");

            iocItem
                    .after("ioc_container")
                    .process(() -> { });

            bootstrap.add(iocItem);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
