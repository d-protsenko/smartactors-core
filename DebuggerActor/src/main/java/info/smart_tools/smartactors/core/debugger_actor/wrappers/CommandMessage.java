package info.smart_tools.smartactors.core.debugger_actor.wrappers;

import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

/**
 * Wrapper interface for debugger command messages.
 */
public interface CommandMessage {
    /**
     * @return name of the command to execute
     * @throws ReadValueException if any error occurs
     */
    String getCommand() throws ReadValueException;

    /**
     * @param sessionId    the session identifier to store in the message
     * @throws ChangeValueException if any error occurs
     */
    void setSessionId(final String sessionId) throws ChangeValueException;

    /**
     * @return identifier of debugger session associated with this message
     * @throws ReadValueException if any error occurs
     */
    String getSessionId() throws ReadValueException;

    /**
     * @return arguments to execute the command with
     * @throws ReadValueException if any error occurs
     */
    Object getCommandArguments() throws ReadValueException;

    /**
     * @param object    result of command execution
     * @throws ChangeValueException if any error occurs
     */
    void setCommandResult(final Object object) throws ChangeValueException;
}
