package info.smart_tools.smartactors.core.debugger_actor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.debugger_actor.exceptions.CommandExecutionException;
import info.smart_tools.smartactors.core.debugger_actor.exceptions.SessionNotFoundException;
import info.smart_tools.smartactors.core.debugger_actor.interfaces.IDebuggerCommand;
import info.smart_tools.smartactors.core.debugger_actor.interfaces.IDebuggerSession;
import info.smart_tools.smartactors.core.debugger_actor.wrappers.CommandMessage;
import info.smart_tools.smartactors.core.debugger_actor.wrappers.DebuggableMessage;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 */
public class DebuggerActor {
    private final Map<String, IDebuggerSession> sessions = new HashMap<>();
    private final Map<String, IDebuggerCommand> globalCommands = new HashMap<>();

    {
        globalCommands.put("newSession",
                arg -> {
            String id = UUID.randomUUID().toString();

            try {
                // TODO: Pass address of this actor
                IDebuggerSession session = IOC.resolve(Keys.getOrAdd("debugger session"), id, arg);

                sessions.put(id, session);
            } catch (ResolutionException e) {
                throw new CommandExecutionException(e);
            }

            return id;
        });

        globalCommands.put("closeSession",
                arg -> {
            String id = (String) arg;

            if (!sessions.containsKey(id)) {
                throw new CommandExecutionException(
                        MessageFormat.format("There is no debugger session with id=''{0}}''.", id));
            }

            sessions.remove(id).close();

            return null;
        });

        globalCommands.put("listSessions",
                arg -> new ArrayList<>(sessions.keySet()));
    }

    /**
     * Execute debugger command.
     *
     * @param message    the message containing command name and arguments
     * @throws ReadValueException if error occurs reading command name, session identifier or command arguments from message
     * @throws ChangeValueException if error occurs saving command result
     * @throws InvalidArgumentException if command argument is not valid
     * @throws CommandExecutionException if error occurs processing a global command
     * @throws SessionNotFoundException if cannot find a session to execute command within
     */
    public void execute(final CommandMessage message)
            throws ReadValueException, ChangeValueException, CommandExecutionException, InvalidArgumentException,
                SessionNotFoundException {
        String command = message.getCommand();

        if (globalCommands.containsKey(command)) {
            Object args = message.getCommandArguments();

            if (null == args) {
                args = message.getSessionId();
            }

            message.setCommandResult(globalCommands.get(command).execute(args));
        } else {
            String sessionId = message.getSessionId();
            Object args = message.getCommandArguments();

            message.setCommandResult(getSession(sessionId).executeCommand(command, args));
        }
    }

    /**
     * Method the {@link info.smart_tools.smartactors.core.debugger_actor.interfaces.IDebuggerSequence} inserts between any two targets of
     * the sequence.
     *
     * @param message the message being debugged
     * @throws ReadValueException if error occurs reading values from message
     * @throws SessionNotFoundException if cannot find the session the message is associated with
     */
    public void interrupt(final DebuggableMessage message)
            throws ReadValueException, SessionNotFoundException {
        getSession(message.getSessionId()).handleInterrupt(message.getProcessor());
    }

    private IDebuggerSession getSession(final String sessionId) throws SessionNotFoundException {
        if (!sessions.containsKey(sessionId)) {
            throw new SessionNotFoundException(sessionId);
        }

        return sessions.get(sessionId);
    }
}