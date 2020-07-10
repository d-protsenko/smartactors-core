package info.smart_tools.smartactors.base.interfaces.iaction;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.FunctionExecutionException;

/**
 * Interface IFunction.
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
public interface IFunction<T, R> {

    /**
     * Executes this function to the given argument.
     * @param t the function argument
     * @return the function result
     * @throws FunctionExecutionException if any errors occurred
     * @throws InvalidArgumentException if incoming argument are incorrect
     */
    R execute(T t)
            throws FunctionExecutionException, InvalidArgumentException;;
}
