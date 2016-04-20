package info.smart_tools.smartactors.core.create_new_instance_strategy;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;

import java.util.function.Function;

/**
 * Implementation of {@link IResolveDependencyStrategy}
 * <pre>
 * Strategy allows to create new instances of specified classes
 * </pre>
 */
class CreateNewInstanceStrategy implements IResolveDependencyStrategy {

    /**
     * Local function for creation new instances of classes
     */
    private Function<Object[], Object> creationFunction;

    /**
     * Default constructor - prohibited
     */
    private CreateNewInstanceStrategy() {
    }

    /**
     * Class constructor
     * Create class instance with initialize private function property
     * @param func function for creation new instances of classes
     */
    CreateNewInstanceStrategy(final Function<Object[], Object> func) {
        this.creationFunction = func;
    }

    /**
     * Represent new instance of class by given param
     * @param args needed parameters for resolve dependency
     * @return instance of object
     * @throws ResolveDependencyStrategyException if any errors occurred
     */
    public Object resolve(final Object ... args)
            throws ResolveDependencyStrategyException {
        try {
            return creationFunction.apply(args);
        } catch (Exception e) {
            throw new ResolveDependencyStrategyException("Object resolution failed.", e);
        }
    }
}
