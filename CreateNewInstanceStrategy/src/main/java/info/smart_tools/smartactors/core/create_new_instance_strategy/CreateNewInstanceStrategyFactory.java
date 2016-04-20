package info.smart_tools.smartactors.core.create_new_instance_strategy;


import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IStrategyFactory;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.StrategyFactoryException;

import java.util.function.Function;

/**
 * Implementation of {@link IStrategyFactory}
 */
public class CreateNewInstanceStrategyFactory implements IStrategyFactory {

    /**
     * Create instance of {@link IResolveDependencyStrategy}
     * @param obj needed parameters for creation
     * @return new instance of {@link IResolveDependencyStrategy}
     * @throws StrategyFactoryException if any errors occurred
     */
    public IResolveDependencyStrategy createStrategy(final Object obj)
            throws StrategyFactoryException {
        try {
            Object [] args = (Object[]) obj;

            return new CreateNewInstanceStrategy(null);
        } catch (Exception e) {
            throw new StrategyFactoryException("Failed to create instance of IResolveDependencyStrategy.", e);
        }
    }
}
