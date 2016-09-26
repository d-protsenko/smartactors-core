package info.smart_tools.smartactors.core.debugger_actor.interfaces;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.debugger_actor.exceptions.CommandExecutionException;

/**
 *
 */
@FunctionalInterface
public interface IDebuggerCommand {
    /**
     * Execute this command.
     *
     * @param args    arguments of the command
     * @return result of command execution
     * @throws InvalidArgumentException if arguments are not valid
     * @throws CommandExecutionException if execution fails because of any error
     */
    Object execute(final Object args) throws InvalidArgumentException, CommandExecutionException;
}
