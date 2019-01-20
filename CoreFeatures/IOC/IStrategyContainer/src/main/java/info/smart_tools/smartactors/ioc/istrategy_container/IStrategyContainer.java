package info.smart_tools.smartactors.ioc.istrategy_container;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.ioc.istrategy_container.exception.StrategyContainerException;

/**
 * StrategyContainer interface
 * Provides methods for resolve and register dependency
 * {@link IResolutionStrategy}
 * by object unique identifier
 */
public interface IStrategyContainer {

    /**
     * Resolve dependency of {@link IResolutionStrategy} by unique object identifier
     * @param key unique object identifier
     * @return instance of {@link IResolutionStrategy}
     * @throws StrategyContainerException if any error occurred
     */
    IResolutionStrategy resolve(final Object key)
            throws StrategyContainerException;

    /**
     * Register new dependency of {@link IResolutionStrategy} by unique object identifier
     * @param key unique object identifier
     * @param strategy instance of {@link IResolutionStrategy}
     * @throws StrategyContainerException  if any error occurred
     */
    void register(final Object key, final IResolutionStrategy strategy)
            throws StrategyContainerException;

    /**
     * Remove existing dependency of {@link IResolutionStrategy} by unique object identifier
     * @param key unique object identifier
     * @throws StrategyContainerException  if any error occurred
     */
    void remove(final Object key)
            throws StrategyContainerException;
}
