package info.smart_tools.smartactors.base.interfaces.iaction;

import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;

/**
 * Interface IBiAction
 * @param <T1> type of first acting object
 * @param <T2> type of second acting object
 */
public interface IBiAction<T1, T2> {

    /**
     * Action for acting object with two parameters
     * @param firstActingObject first acting object
     * @param secondActingObject second acting object
     * @throws ActionExecuteException if any errors occurred
     * @throws InvalidArgumentException if incoming argument are incorrect
     */
    void execute(final T1 firstActingObject, final T2 secondActingObject)
            throws ActionExecuteException, InvalidArgumentException;
}
