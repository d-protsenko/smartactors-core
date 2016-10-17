package info.smart_tools.smartactors.debugger.session_impl;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.debugger.interfaces.IDebuggerCommand;
import info.smart_tools.smartactors.debugger.interfaces.IDebuggerSequence;
import info.smart_tools.smartactors.debugger.interfaces.IDebuggerSession;
import info.smart_tools.smartactors.debugger.interfaces.exceptions.CommandExecutionException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of a debugger session.
 */
public class DebuggerSessionImpl implements IDebuggerSession {
    private final IFieldName sessionIdFieldName;

    private final Map<String, IDebuggerCommand> commands = new HashMap<>();
    private final String id;
    private final Object debuggerAddress;

    private IDebuggerSequence sequence;
    private IMessageProcessor processor;

    private IObject message;
    private IReceiverChain mainChain;

    /**
     * The constructor.
     *
     * @param id                 identifier of this session
     * @param debuggerAddress    address of the debugger actor
     * @throws ResolutionException if resolution of any dependency fails
     */
    public DebuggerSessionImpl(final String id, final Object debuggerAddress)
            throws ResolutionException {
        this.id = id;
        this.debuggerAddress = debuggerAddress;

        sessionIdFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "sessionId");

        commands.put("start", this::startDebugging);
    }

    @Override
    public void handleInterrupt(final IMessageProcessor messageProcessor) {

    }

    @Override
    public Object executeCommand(final String name, final Object args) throws InvalidArgumentException, CommandExecutionException {
        if (!commands.containsKey(name)) {
            throw new InvalidArgumentException(MessageFormat.format("No such debugger command: ''{0}''.", name));
        }

        return commands.get(name).execute(args);
    }

    @Override
    public void close() {

    }

    private Object startDebugging(final Object arg)
            throws CommandExecutionException {
        if (message == null || mainChain == null || sequence != null) {
            throw new CommandExecutionException("Can not start debugging.");
        }

        try {
            IMessageProcessingSequence innerSequence = IOC.resolve(
                    Keys.getOrAdd(IMessageProcessingSequence.class.getCanonicalName()), 5, mainChain);

            // TODO: Pass debugger address
            sequence = IOC.resolve(Keys.getOrAdd("new debugger sequence"), innerSequence);

            Object taskQueue = IOC.resolve(Keys.getOrAdd("task_queue"));

            processor = IOC.resolve(Keys.getOrAdd(IMessageProcessor.class.getCanonicalName()), taskQueue, sequence);

            IObject context = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

            context.setValue(sessionIdFieldName, id);

            processor.process(message, context);
        } catch (ResolutionException | ChangeValueException | InvalidArgumentException e) {
            throw new CommandExecutionException(e);
        }

        return null;
    }
}
