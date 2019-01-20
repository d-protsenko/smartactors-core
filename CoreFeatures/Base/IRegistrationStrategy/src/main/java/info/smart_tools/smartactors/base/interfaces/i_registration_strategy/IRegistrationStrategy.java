package info.smart_tools.smartactors.base.interfaces.i_registration_strategy;

import info.smart_tools.smartactors.base.interfaces.i_registration_strategy.exception.RegistrationStrategyException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;

/**
 * Interface to add or delete strategies from strategies
 */
public interface IRegistrationStrategy {

    /**
     * Method for register new strategy to key
     *
     * @param key   key for using strategy
     * @param value {@link IResolveDependencyStrategy} object, which should register by the key
     * @throws RegistrationStrategyException if any errors occurred
     */
    void register(final Object key, final IResolveDependencyStrategy value) throws RegistrationStrategyException;

    /**
     * Method for unregistering strategy from key
     *
     * @param key key of the unregistering strategy
     * @throws RegistrationStrategyException if any errors occurred
     */
    void unregister(final Object key) throws RegistrationStrategyException;

}
