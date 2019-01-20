package info.smart_tools.smartactors.version_management.versioned_recursive_strategy_container;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.class_management.interfaces.imodule.IModule;
import info.smart_tools.smartactors.class_management.module_manager.ModuleManager;
import info.smart_tools.smartactors.ioc.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.ioc.istrategy_container.exception.StrategyContainerException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link IStrategyContainer}
 * <p>
 * Simple key-value storage
 * <ul>
 *     <li>key is a unique object identifier</li>
 *     <li>value is a instance of {@link IResolutionStrategy}</li>
 * </ul>
 * </p>
 * <p>
 * Stores the link to the parent container to make the recursive resolving
 * when the strategy doesn't exist in the current container.
 * </p>
 */
public class StrategyContainer implements IStrategyContainer {

    private IStrategyContainer parent;
    /**
     * Local storage
     */
    private Map<Object, Map<IModule, IResolutionStrategy>> strategyStorage = new ConcurrentHashMap<>();

    /**
     *  Constructs the container.
     *  @param parent   parent container to do the recursive resolve, can be null for empty parent
     */
    public StrategyContainer(final IStrategyContainer parent) {
        this.parent = parent;
    }

    /**
     * Resolve {@link IResolutionStrategy} by given unique object identifier.
     * Asks parent strategy if this container doesn't have a strategy for the key.
     * @param key unique object identifier
     * @return instance of {@link IResolutionStrategy}
     * @throws StrategyContainerException if any errors occurred
     */
    public IResolutionStrategy resolve(final Object key)
            throws StrategyContainerException {
        IResolutionStrategy strategy = null;
        Map<IModule, IResolutionStrategy> strategyVersions = strategyStorage.get(key);
        if (strategyVersions != null) {
            strategy = ModuleManager.getFromMap(strategyVersions);
        }
        if (strategy == null && parent != null) {
            strategy = parent.resolve(key);    // ask parent ONLY AFTER local resolution failed
        }
        return strategy;
    }

    /**
     * Register new dependency of {@link IResolutionStrategy} instance by unique object identifier
     * @param key unique object identifier
     * @param strategy instance of {@link IResolutionStrategy}
     * @throws StrategyContainerException if any error occurred
     */
    public void register(final Object key, final IResolutionStrategy strategy)
            throws StrategyContainerException {
        Map<IModule, IResolutionStrategy> strategyVersions = strategyStorage.get(key);
        if (strategyVersions == null) {
            strategyVersions = new ConcurrentHashMap<>();
            strategyStorage.put(key, strategyVersions);
        }
        ModuleManager.putToMap(strategyVersions, strategy);
    }

    /**
     * Remove existing dependency of {@link IResolutionStrategy} by unique object identifier.
     * Note remove is done only for this container,
     * the following call to {@link #resolve(Object)} may return the strategy from the parent container.
     * @param key unique object identifier
     * @throws StrategyContainerException  if any error occurred
     */
    public void remove(final Object key)
            throws StrategyContainerException {
        Map<IModule, IResolutionStrategy> strategyVersions = strategyStorage.get(key);
        if (strategyVersions != null) {
            ModuleManager.removeFromMap(strategyVersions);
            if (strategyVersions.size() == 0) {
                strategyStorage.remove(key);
            }
        }
    }
}
