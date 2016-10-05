package info.smart_tools.smartactors.message_bus.message_bus_handler;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.message_bus.interfaces.imessage_bus_handler.IMessageBusHandler;
import info.smart_tools.smartactors.message_bus.interfaces.imessage_bus_handler.exception.MessageBusHandlerException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MessageBusHandler implements IMessageBusHandler {

    private final IFieldName replyToFieldName;
    private final IFieldName finalActionsFieldName;

    private final IQueue<ITask> taskQueue;
    private final int stackDepth;
    private final IReceiverChain chain;
    private final IAction<IObject> replyAction;

    /**
     *
     * @param taskQueue Queue of the tasks
     * @param stackDepth Stack depth of the {@link IMessageProcessor}
     * @param receiverChain the chain for processing incoming message
     * @param finalAction the final action for
     * @throws InvalidArgumentException if there is invalid arguments
     */
    public MessageBusHandler(final IQueue<ITask> taskQueue, final int stackDepth, final IReceiverChain receiverChain, final IAction<IObject> finalAction)
            throws InvalidArgumentException, ResolutionException {
        if (null == taskQueue) {
            throw new InvalidArgumentException("Task queue should not be null.");
        }
        if (stackDepth < 0) {
            throw new InvalidArgumentException("Stack depth should be positive number.");
        }
        if (null == receiverChain) {
            throw new InvalidArgumentException("ReceiverChain should not be null.");
        }
        if (null == finalAction)  {
            throw new InvalidArgumentException("FinalAction should not be null.");
        }
        this.stackDepth = stackDepth;
        this.taskQueue = taskQueue;
        this.chain = receiverChain;
        this.replyAction = finalAction;

        this.finalActionsFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "finalActions");
        this.replyToFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "messageBusReplyTo");
    }

    @Override
    public void handle(final IObject message)
            throws MessageBusHandlerException {
        try {
            IMessageProcessor messageProcessor = resolveMessageProcessor(this.chain);
            IObject context = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.getCanonicalName())
            );
            messageProcessor.process(message, context);
        } catch (ResolutionException | InvalidArgumentException | ChangeValueException e) {
            throw new MessageBusHandlerException("Failed to handle message to MessageBus.", e);
        }
    }

    @Override
    public void handle(final IObject message, final Object chainName)
            throws MessageBusHandlerException {
        try {
            IReceiverChain chain = resolveChain(chainName);
            IMessageProcessor messageProcessor = resolveMessageProcessor(chain);
            IObject context = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.getCanonicalName())
            );
            messageProcessor.process(message, context);
        } catch (ResolutionException | ChainNotFoundException | InvalidArgumentException | ChangeValueException e) {
            throw new MessageBusHandlerException("Failed to handle message to MessageBus.", e);
        }
    }

    @Override
    public void handleForReply(final IObject message, final Object replyToChainName)
            throws MessageBusHandlerException {
        try {
            IMessageProcessor messageProcessor = resolveMessageProcessor(this.chain);
            messageProcessor.process(message, createAndFillContext(replyToChainName));
        } catch (ResolutionException | ChangeValueException | InvalidArgumentException e) {
            throw new MessageBusHandlerException("Failed to handle message to MessageBus.", e);
        }
    }

    @Override
    public void handleForReply(final IObject message, final Object chainName, final Object replyToChainName)
            throws MessageBusHandlerException {
        try {
            IReceiverChain chain = resolveChain(chainName);
            IMessageProcessor messageProcessor = resolveMessageProcessor(chain);
            messageProcessor.process(message, createAndFillContext(replyToChainName));
        } catch (ResolutionException | ChainNotFoundException | ChangeValueException | InvalidArgumentException e) {
            throw new MessageBusHandlerException("Failed to handle message to MessageBus.", e);
        }
    }

    private IReceiverChain resolveChain(final Object chainName)
            throws ResolutionException, ChainNotFoundException {
        Object chainId = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyStorage(), "chain_id_from_map_name"), chainName
        );
        IChainStorage chainStorage = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyStorage(), IChainStorage.class.getCanonicalName())
        );

        return chainStorage.resolve(chainId);
    }

    private IMessageProcessor resolveMessageProcessor(final IReceiverChain mpChain) throws ResolutionException {
        IMessageProcessingSequence processingSequence = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyStorage(), IMessageProcessingSequence.class.getCanonicalName()),
                this.stackDepth,
                mpChain
        );
        return IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyStorage(), IMessageProcessor.class.getCanonicalName()),
                this.taskQueue,
                processingSequence
        );
    }

    private IObject createAndFillContext(final Object replyToChainName)
            throws InvalidArgumentException, ResolutionException, ChangeValueException {
        IObject context = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.getCanonicalName())
        );
        List<IAction> actionList = new ArrayList<>();
        actionList.add(this.replyAction);
        context.setValue(this.finalActionsFieldName, actionList);
        context.setValue(this.replyToFieldName, replyToChainName);

        return context;
    }
}
