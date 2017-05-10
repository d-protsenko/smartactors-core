package info.smart_tools.smartactors.ioc_strategy_pack_plugins.resolve_iobject_strategies_plugin;

import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.IAdditionDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.exception.AdditionDependencyStrategyException;
import info.smart_tools.smartactors.base.interfaces.iaction.IBiFunction;
import info.smart_tools.smartactors.base.interfaces.iaction.IFunction;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.strategy.strategy_storage_with_cache_strategy.StrategyStorageWithCacheStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_iobject_strategies.MapToIObjectResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_iobject_strategies.StringToIObjectResolveDependencyStrategy;

import java.util.Map;

/**
 * Plugin registers resolve by type strategy for IObject and fills it by
 * concrete converting IObject strategies from different classes (map, string)
 */
public class ResolveIObjectByTypeStrategiesPlugin implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap element
     * @throws InvalidArgumentException if bootstrap is null
     */
    public ResolveIObjectByTypeStrategiesPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) throws InvalidArgumentException {
        if (null == bootstrap) {
            throw new InvalidArgumentException("Incoming argument should not be null.");
        }
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("ResolveIObjectByTypeStrategiesPlugin");
            item
                .after("IOC")
                .process(() -> {
                    try {
                        IFunction argToKey = arg -> arg.getClass();
                        IBiFunction findValueByArgument = (map, arg) -> {
                            IResolveDependencyStrategy strategy = null;
                            for (Map.Entry<Class, IResolveDependencyStrategy> entry : ((Map<Class, IResolveDependencyStrategy>) map).entrySet()) {
                                if (entry.getKey().isInstance(arg)) {
                                    strategy = entry.getValue();

                                    break;
                                }
                            }
                            return strategy;
                        };

                        IKey typeStrategy = Keys.getOrAdd(IObject.class.getCanonicalName() + "convert");
//                        ResolveByTypeStrategy resolveStrategy = new ResolveByTypeStrategy();
                        IResolveDependencyStrategy resolveStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                        ((IAdditionDependencyStrategy) resolveStrategy).register(Map.class, new MapToIObjectResolveDependencyStrategy());
                        ((IAdditionDependencyStrategy) resolveStrategy).register(String.class, new StringToIObjectResolveDependencyStrategy());
                        IOC.register(typeStrategy, resolveStrategy);
                    } catch (ResolutionException e) {
                        throw new ActionExecuteException("ResolveIObjectByTypeStrategies plugin can't load: can't get ResolveIObjectByTypeStrategies key", e);
                    } catch (RegistrationException | AdditionDependencyStrategyException e) {
                        throw new ActionExecuteException("ResolveIObjectByTypeStrategies plugin can't load: can't register new strategy", e);
                    }
                });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load resolve iobject by type strategies plugin", e);
        }
    }
}
