package info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy;

import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.exception.AdditionDependencyStrategyException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;

/**
 * Interface to add or delete strategies from strategies
 */
public interface IAdditionDependencyStrategy {

    /**
     * Method for register new strategy at strategy
     *
     * @param key   key for using strategy
     * @param value {@link IResolveDependencyStrategy} object, which should register by the key
     * @throws AdditionDependencyStrategyException if any errors occurred
     */
    void register(final Object key, final IResolveDependencyStrategy value) throws AdditionDependencyStrategyException;

    /**
     * Method for remove strategy from strategy
     *
     * @param key key of the deletion strategy
     * @throws AdditionDependencyStrategyException if any errors occurred
     */
    void remove(final Object key) throws AdditionDependencyStrategyException;

}
