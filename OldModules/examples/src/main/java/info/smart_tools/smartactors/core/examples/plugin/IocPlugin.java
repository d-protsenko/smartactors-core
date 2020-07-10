package info.smart_tools.smartactors.core.examples.plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.ioc.string_ioc_key.Key;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;

/**
 * The plugin which mimics the IOC initialization and provides "IOC" dependency.
 */
public class IocPlugin implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Creates the plugin.
     * @param bootstrap bootstrap where this plugin puts it's {@link IBootstrapItem}
     */
    public IocPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("IOC");
            item.process(() -> {
                try {
                    Object scopeKey = ScopeProvider.createScope(null);
                    IScope scope = ScopeProvider.getScope(scopeKey);
                    ScopeProvider.setCurrentScope(scope);
                    scope.setValue(IOC.getIocKey(), new StrategyContainer());
                    IOC.register(IOC.getKeyForKeyByNameStrategy(), new ResolveByNameIocStrategy(
                            (a) -> {
                                try {
                                    return new Key((String) a[0]);
                                } catch (InvalidArgumentException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                System.out.println("IOC initialized");
            });
            bootstrap.add(item);
        } catch (Exception e) {
            throw new PluginException("Could not load IOC plugin", e);
        }
        System.out.println("IOC loaded");
    }

}
