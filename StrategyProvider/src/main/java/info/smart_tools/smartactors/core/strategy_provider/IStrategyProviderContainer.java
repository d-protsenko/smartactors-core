package info.smart_tools.smartactors.core.strategy_provider;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IStrategyFactory;
import info.smart_tools.smartactors.core.strategy_provider.exception.StrategyProviderException;

/**
 * Interface of StrategyProvider
 */
public interface IStrategyProviderContainer {

    /**
     * Create new instance of specific {@link IResolveDependencyStrategy}
     * @param key unique identifier of {@link IResolveDependencyStrategy}
     * @param args parameters needed for creation new instance of {@link IResolveDependencyStrategy}
     * @return new instance of {@link IResolveDependencyStrategy}
     * @throws StrategyProviderException if any errors occurred
     */
    IResolveDependencyStrategy createStrategy(final Object key, final Object ... args)
            throws StrategyProviderException;

    /**
     * Add new instance of {@link IStrategyFactory} to the Strategy Provider local storage
     * @param key unique identifier for {@link IStrategyFactory}
     * @param factory instance of {@link IStrategyFactory}
     * @throws StrategyProviderException if any errors occurred
     */
    void addStrategyFactory(final Object key, final IStrategyFactory factory) throws StrategyProviderException;

    /**
     * Remove instance of {@link IStrategyFactory} from Strategy Provider local storage
     * @param key unique identifier of {@link IStrategyFactory}
     * @throws StrategyProviderException if any errors occurred
     */
    void removeStrategyFactory(final Object key) throws StrategyProviderException;
}