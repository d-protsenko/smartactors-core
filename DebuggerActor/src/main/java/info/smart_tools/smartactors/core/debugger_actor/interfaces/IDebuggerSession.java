package info.smart_tools.smartactors.core.debugger_actor.interfaces;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;

/**
 *
 */
public interface IDebuggerSession {
    /**
     * Called by debugger when it receives message associated with this session at {@code interrupt} method.
     *
     * @param messageProcessor    the message processor processing the debuggable message
     */
    void handleInterrupt(final IMessageProcessor messageProcessor);

    /**
     * Execute a command in this session.
     *
     * @param name    name of the command to execute
     * @param args    arguments of the command
     * @return result of command execution
     * @throws InvalidArgumentException if command arguments are not valid
     */
    Object executeCommand(final String name, final Object args) throws InvalidArgumentException;

    /**
     * Close this session.
     */
    void close();
}
