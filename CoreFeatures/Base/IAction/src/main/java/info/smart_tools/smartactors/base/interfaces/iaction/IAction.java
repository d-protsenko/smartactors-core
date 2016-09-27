package info.smart_tools.smartactors.base.interfaces.iaction;

import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;

/**
 * Interface IAction.
 * @param <T> type of acting object
 */
public interface IAction<T> {

    /**
     * Action for acting object
     * @param actingObject acting object
     * @throws ActionExecuteException if any errors occurred
     * @throws InvalidArgumentException if incoming argument are incorrect
     */
    void execute(final T actingObject)
            throws ActionExecuteException, InvalidArgumentException;
}
