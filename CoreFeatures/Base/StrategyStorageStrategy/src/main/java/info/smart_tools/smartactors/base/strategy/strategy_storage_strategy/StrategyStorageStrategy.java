package info.smart_tools.smartactors.base.strategy.strategy_storage_strategy;

import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.IAdditionDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.exception.AdditionDependencyStrategyException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Strategy to storage some specific strategies united by a common purpose
 */
public class StrategyStorageStrategy implements IResolveDependencyStrategy, IAdditionDependencyStrategy {

    /**
     * Strategy storage
     */
    private ConcurrentMap<Object, IResolveDependencyStrategy> strategyStorage;

    /**
     * Default constructor
     */
    public StrategyStorageStrategy() {
        this.strategyStorage = new ConcurrentHashMap<>();
    }

    @Override
    public <T> T resolve(Object... args) throws ResolveDependencyStrategyException {
        try {
            IResolveDependencyStrategy strategy = this.strategyStorage.get(args[0]);

            return null == strategy ? null : strategy.resolve(args);
        } catch (Exception e) {
            throw new ResolveDependencyStrategyException("Object resolution failed.", e);
        }
    }

    @Override
    public void register(Object key, IResolveDependencyStrategy value)
            throws AdditionDependencyStrategyException {
        this.strategyStorage.put(key, value);
    }

    @Override
    public void remove(Object key)
            throws AdditionDependencyStrategyException {
        this.strategyStorage.remove(key);
    }
}
