package info.smart_tools.smartactors.core.iresolve_dependency_strategy;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.StrategyFactoryException;

/**
 * IStrategyFactory
 */
public interface IStrategyFactory {

    /**
     * Factory method for create new instance if {@link IResolveDependencyStrategy}
     * @param args needed parameters for creation
     * @return instance of {@link IResolveDependencyStrategy}
     * @throws StrategyFactoryException if any errors occurred
     */
    IResolveDependencyStrategy createStrategy(final Object ... args) throws StrategyFactoryException;
}
