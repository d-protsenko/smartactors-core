package info.smart_tools.smartactors.base.strategy.create_new_instance_strategy;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.istrategy.IStrategy;
import info.smart_tools.smartactors.base.interfaces.istrategy.exception.StrategyException;

import java.util.function.Function;

/**
 * Implementation of {@link IStrategy}
 * <pre>
 * Strategy allows to create new instances of specified classes
 * </pre>
 *
 * @since 1.8
 */
@Deprecated
public class CreateNewInstanceStrategy implements IStrategy {

    /**
     * Local function for creation new instances of classes
     */
    private Function<Object[], Object> creationFunction;

    /**
     * Class constructor
     * Create instance of {@link CreateNewInstanceStrategy}
     * @param func function to create new object instance
     * @throws InvalidArgumentException if any errors occurred
     */
    public CreateNewInstanceStrategy(final Function<Object[], Object> func)
            throws InvalidArgumentException {
        if (null == func) {
            throw new InvalidArgumentException("Incoming argument should not be null.");
        }
        this.creationFunction = func;
    }

    /**
     * Represent new instance of class by given param
     * @param <T> type of object
     * @param args needed parameters for resolve dependency
     * @return instance of object
     * @throws StrategyException if any errors occurred
     */
    public <T> T resolve(final Object ... args)
            throws StrategyException {
        try {
            return (T) creationFunction.apply(args);
        } catch (Exception e) {
            throw new StrategyException("Object resolution failed.", e);
        }
    }
}
