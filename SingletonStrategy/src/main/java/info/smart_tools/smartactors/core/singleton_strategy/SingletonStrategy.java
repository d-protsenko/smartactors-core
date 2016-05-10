package info.smart_tools.smartactors.core.singleton_strategy;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;

/**
 * Implementation of {@link IResolveDependencyStrategy}
 * <pre>
 * This strategy realize singletone pattern for resolving class
 * </pre>
 */
public class SingletonStrategy implements IResolveDependencyStrategy {

    /**
     * instance of Object
     */
    private Object classInstance;

    /**
     * Class constructor
     * Create strategy instance with initialized object
     * @param args singleton instance
     */
    public SingletonStrategy(final Object ... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Singleton constructor should has only one arg.");
        }
        this.classInstance = args[0];
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
