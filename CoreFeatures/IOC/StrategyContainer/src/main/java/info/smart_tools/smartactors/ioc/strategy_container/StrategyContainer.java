package info.smart_tools.smartactors.ioc.strategy_container;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.ioc.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.ioc.istrategy_container.exception.StrategyContainerException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link IStrategyContainer}
 * <pre>
 * Simple key-value storage
 *  - key is a unique object identifier
 *  - value is a instance of {@link IResolutionStrategy}
 * </pre>
 */
public class StrategyContainer implements IStrategyContainer {

    /**
     * Local storage
     */
    private Map<Object, IResolutionStrategy> strategyStorage = new ConcurrentHashMap<Object, IResolutionStrategy>();

    /**
     * Resolve {@link IResolutionStrategy} by given unique object identifier
     * @param key unique object identifier
     * @return instance of {@link IResolutionStrategy}
     * @throws StrategyContainerException if any errors occurred
     */
    public IResolutionStrategy resolve(final Object key)
            throws StrategyContainerException {
        return strategyStorage.get(key);
    }

    /**
     * Register new dependency of {@link IResolutionStrategy} instance by unique object identifier
     * @param key unique object identifier
     * @param strategy instance of {@link IResolutionStrategy}
     * @throws StrategyContainerException if any error occurred
     */
    public void register(final Object key, final IResolutionStrategy strategy)
            throws StrategyContainerException {
        strategyStorage.put(key, strategy);
    }

    /**
     * Remove existing dependency of {@link IResolutionStrategy} by unique object identifier
     * @param key unique object identifier
     * @throws StrategyContainerException if any error occurred
     */
    public void remove(final Object key)
            throws StrategyContainerException {
        strategyStorage.remove(key);
    }
}
