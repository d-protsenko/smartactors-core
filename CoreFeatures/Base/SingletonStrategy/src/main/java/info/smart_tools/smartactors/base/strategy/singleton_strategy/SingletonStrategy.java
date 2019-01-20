package info.smart_tools.smartactors.base.strategy.singleton_strategy;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;

/**
 * Implementation of {@link IResolutionStrategy}
 * <pre>
 * This strategy realize singletone pattern for resolving class
 * </pre>
 */
public class SingletonStrategy implements IResolutionStrategy {

    /**
     * instance of Object
     */
    private Object classInstance;

    /**
     * Class constructor
     * Create strategy instance with initialized object
     * @param args singleton instance
     * @throws InvalidArgumentException if any errors occurred
     */
    public SingletonStrategy(final Object ... args) throws InvalidArgumentException {
        if (args.length != 1) {
            throw new InvalidArgumentException("Singleton constructor should has only one arg.");
        }
        this.classInstance = args[0];
    }

    /**
     * Represent instance of class
     * @param <T> type of object
     * @param args needed parameters for resolve dependency
     * @return instance of object
     * @throws ResolutionStrategyException if any errors occurred
     */
    public <T> T resolve(final Object... args)
            throws ResolutionStrategyException {
        return (T) classInstance;
    }
}
