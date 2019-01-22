package info.smart_tools.smartactors.ioc_strategy_pack.resolve_by_type_strategy;

import info.smart_tools.smartactors.base.interfaces.iregistration_strategy.IRegistrationStrategy;
import info.smart_tools.smartactors.base.interfaces.iregistration_strategy.exception.RegistrationStrategyException;
import info.smart_tools.smartactors.base.interfaces.istrategy.IStrategy;
import info.smart_tools.smartactors.base.interfaces.istrategy.exception.StrategyException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Strategy for resolving by type
 *
 * @deprecated use {@link info.smart_tools.smartactors.base.strategy.strategy_storage_with_cache_strategy.StrategyStorageWithCacheStrategy} instead.
 *
 */
@Deprecated
public class ResolveByTypeStrategy implements IStrategy, IRegistrationStrategy {
    /**
     * Specific strategies for resolve
     */
    private ConcurrentMap<Class, IStrategy> resolveStrategies;
    private ConcurrentMap<Class, IStrategy> cacheStrategiesMap;


    /**
     * Default constructor
     */
    public ResolveByTypeStrategy() {
        this.resolveStrategies = new ConcurrentHashMap<>();
        this.cacheStrategiesMap = new ConcurrentHashMap<>();
    }

    /**
     * Put strategy for specific output type
     * @param key the key for output type
     * @param strategy the strategy for specific output type
     */
    @Override
    public void register(final Object key, final IStrategy strategy) {
        cacheStrategiesMap.remove((Class) key);
        resolveStrategies.put((Class) key, strategy);
    }

    /**
     * Remove strategy for specific output type
     * @param key the key for output type
     */
    @Override
    public void unregister(final Object key) throws RegistrationStrategyException {
        resolveStrategies.remove(key);
    }

    @Override
    public <T> T resolve(final Object... args) throws StrategyException {
        try {
            IStrategy strategy = cacheStrategiesMap.get(args[0].getClass());
            if (strategy == null) {
                for (Map.Entry<Class, IStrategy> entry : resolveStrategies.entrySet()) {
                    if (entry.getKey().isInstance(args[0])) {
                        strategy = entry.getValue();
                        cacheStrategiesMap.put(args[0].getClass(), strategy);
                        break;
                    }
                }
            }

            Object result = strategy.resolve(args[0]);
            return (T) result;
        } catch (Exception e) {
            throw new StrategyException("Object resolution failed.", e);
        }
    }
}
