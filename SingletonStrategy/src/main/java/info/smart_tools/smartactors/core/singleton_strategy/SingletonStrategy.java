package info.smart_tools.smartactors.core.singleton_strategy;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;

/**
 * Implementation of {@link IResolveDependencyStrategy}
 * <pre>
 * This strategy realize singletone pattern for resolving class
 * </pre>
 */
class SingletonStrategy implements IResolveDependencyStrategy {

    /**
     * instance of Object
     */
    private Object classInstance;

    /**
     * Default constructor - prohibited
     */
    private SingletonStrategy() {
    }

    /**
     * Class constructor
     * Create strategy instance with initialized object
     * @param classInstance initialized object
     */
    SingletonStrategy(final Object classInstance) {
        this.classInstance = classInstance;
    }

    /**
     * Represent instance of class
     * @param <T> type of object
     * @param args needed parameters for resolve dependency
     * @return instance of object
     * @throws ResolveDependencyStrategyException if any errors occurred
     */
    public <T> T resolve(final Object... args)
            throws ResolveDependencyStrategyException {
        return (T) classInstance;
    }
}
