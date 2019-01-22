package info.smart_tools.smartactors.ioc_strategy_pack_plugins.resolve_iobject_strategies_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iregistration_strategy.IRegistrationStrategy;
import info.smart_tools.smartactors.base.interfaces.iregistration_strategy.exception.RegistrationStrategyException;
import info.smart_tools.smartactors.base.interfaces.iaction.IFunctionTwoArgs;
import info.smart_tools.smartactors.base.interfaces.iaction.IFunction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecutionException;
import info.smart_tools.smartactors.base.interfaces.istrategy.IStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.base.strategy.strategy_storage_with_cache_strategy.StrategyStorageWithCacheStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_iobject_strategies.MapToIObjectStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_iobject_strategies.StringToIObjectStrategy;

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
                        IFunctionTwoArgs findValueByArgument = (map, arg) -> {
                            IStrategy strategy = null;
                            for (Map.Entry<Class, IStrategy> entry : ((Map<Class, IStrategy>) map).entrySet()) {
                                if (entry.getKey().isInstance(arg)) {
                                    strategy = entry.getValue();

                                    break;
                                }
                            }
                            return strategy;
                        };

                        IKey typeStrategy = Keys.resolveByName("info.smart_tools.smartactors.iobject.iobject.IObject" + "convert");
                        IKey expandableTypeStrategy = Keys.resolveByName("expandable_strategy#" + "info.smart_tools.smartactors.iobject.iobject.IObject");
                        IStrategy resolveStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                        ((IRegistrationStrategy) resolveStrategy).register(Map.class, new MapToIObjectStrategy());
                        ((IRegistrationStrategy) resolveStrategy).register(String.class, new StringToIObjectStrategy());
                        IOC.register(typeStrategy, resolveStrategy);
                        IOC.register(expandableTypeStrategy, new SingletonStrategy(resolveStrategy));
                    } catch (ResolutionException e) {
                        throw new ActionExecutionException("ResolveIObjectByTypeStrategies plugin can't load: can't get ResolveIObjectByTypeStrategies key", e);
                    } catch (RegistrationException | RegistrationStrategyException | InvalidArgumentException e) {
                        throw new ActionExecutionException("ResolveIObjectByTypeStrategies plugin can't load: can't register new strategy", e);
                    }
                })
                .revertProcess(() -> {
                    String itemName = "ResolveIObjectByTypeStrategiesPlugin";
                    String keyName = "";

                    try {
                        keyName = "expandable_strategy#" + "info.smart_tools.smartactors.iobject.iobject.IObject";
                        IOC.remove(Keys.resolveByName(keyName));
                    } catch(DeletionException e) {
                        System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                    } catch (ResolutionException e) { }

                    try {
                        keyName = "info.smart_tools.smartactors.iobject.iobject.IObject" + "convert";
                        IOC.remove(Keys.resolveByName(keyName));
                    } catch(DeletionException e) {
                        System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                    } catch (ResolutionException e) { }
                });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load resolve iobject by type strategies plugin", e);
        }
    }
}
