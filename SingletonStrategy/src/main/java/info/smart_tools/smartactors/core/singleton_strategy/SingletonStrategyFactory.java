package info.smart_tools.smartactors.core.singleton_strategy;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IStrategyFactory;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.StrategyFactoryException;

/**
 * Implementation of {@link IStrategyFactory}
 * for {@link SingletonStrategy}
 */
public class SingletonStrategyFactory implements IStrategyFactory {

    /**
     * Create instance of {@link SingletonStrategy}
     * @param obj singleton instance
     * @return new instance of {@link SingletonStrategy}
     * @throws StrategyFactoryException if any errors occurred
     */
    public IResolveDependencyStrategy createStrategy(final Object obj)
            throws StrategyFactoryException {
        try {

            return new SingletonStrategy(obj);
        } catch (Exception e) {
            throw new StrategyFactoryException("Failed to create instance of SingletonStrategy.", e);
        }
    }
}
