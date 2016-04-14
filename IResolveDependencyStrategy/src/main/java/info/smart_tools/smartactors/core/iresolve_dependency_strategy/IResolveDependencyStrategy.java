package info.smart_tools.smartactors.core.iresolve_dependency_strategy;

import info.smart_tools.smartactors.core.iobject.IObject;

/**
 * IResolveDependencyStrategy
 */
public interface IResolveDependencyStrategy {

    /**
     * Resolve dependency by realized strategy
     * @param params needed parameters for resolve dependency
     * @param <T> type of resolved object
     * @return instance of object
     */
    //TODO: need change IObject by wrapper when needed code will be implemented
    <T> T resolve(final IObject params);
}
