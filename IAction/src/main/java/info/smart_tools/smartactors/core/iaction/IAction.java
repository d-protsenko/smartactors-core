package info.smart_tools.smartactors.core.iaction;

import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;

/**
 * Interface IAction
 * @param <T> type of acting object
 */
public interface IAction<T> {

    /**
     * Action for acting object
     * @param actingObject acting object
     * @throws ActionExecuteException if any errors occurred
     */
    void execute(final T actingObject)
            throws ActionExecuteException;
}
