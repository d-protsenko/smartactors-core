package info.smart_tools.smartactors.base.interfaces.iaction;

import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecutionException;

/**
 * Interface IPoorAction.
 * Action without incoming argument.
 */
public interface IPoorAction {
    /**
     * Action without incoming arguments
     * @throws ActionExecutionException if any errors occurred
     */
    void execute()
            throws ActionExecutionException;
}
