package info.smart_tools.smartactors.core.iresolve_dependency_strategy;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;

/**
 * IResolveDependencyStrategy
 */
public interface IResolveDependencyStrategy {

    /**
     * Resolve dependency by realized strategy
     * @param args array of needed parameters for resolve dependency
     * @return T instance of object
     * @throws ResolveDependencyStrategyException if any errors occurred
     */
    //TODO: need change IObject by wrapper when needed code will be implemented
    Object resolve(final Object ... args)
            throws ResolveDependencyStrategyException;
}
