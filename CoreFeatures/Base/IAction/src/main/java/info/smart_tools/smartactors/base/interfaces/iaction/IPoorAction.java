package info.smart_tools.smartactors.base.interfaces.iaction;

import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;

/**
 * Interface IPoorAction.
 * Action without incoming argument.
 */
public interface IPoorAction {
    /**
     * Action without incoming arguments
     * @throws ActionExecuteException if any errors occurred
     */
    void execute()
            throws ActionExecuteException;
}
