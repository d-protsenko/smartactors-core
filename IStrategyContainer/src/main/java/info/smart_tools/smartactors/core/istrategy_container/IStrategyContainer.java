package info.smart_tools.smartactors.core.istrategy_container;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;

/**
 * StrategyContainer interface
 * Provides methods for resolve dependency {@link info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy}
 * by object unique identifier
 */
public interface IStrategyContainer {

    /**
     * Resolve dependency by unique object identifier
     * @param key unique object identifier
     * @return instance of {@link IResolveDependencyStrategy}
     */
    IResolveDependencyStrategy resolve(final Object key);
}
