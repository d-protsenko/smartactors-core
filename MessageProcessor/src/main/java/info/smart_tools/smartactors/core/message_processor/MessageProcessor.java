package info.smart_tools.smartactors.core.message_processor;

import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iaction.IPoorAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.imessage.IMessage;
import info.smart_tools.smartactors.core.imessage_processing_sequence.IMessageProcessingSequence;
import info.smart_tools.smartactors.core.imessage_processing_sequence.exceptions.NoExceptionHandleChainException;
import info.smart_tools.smartactors.core.imessage_receiver.IMessageReceiver;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.iresource_source.exceptions.OutOfResourceException;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;

/**
 * Task that performs on a message actions defined by a message processing sequence.
 *
 * @see IMessageProcessingSequence
 * @see ITask
 */
public class MessageProcessor implements ITask {
    private IObject context;
    private IMessage message;

    private final IQueue<ITask> taskQueue;
    private final IMessageProcessingSequence messageProcessingSequence;

    private final ReceiverCallback receiverCallback;
    private final ReEnqueueAction reEnqueueAction;

    /**
     * Action that enqueues this task.
     */
    private class ReEnqueueAction implements IPoorAction {
        @Override
        public void execute() throws ActionExecuteException {
            MessageProcessor.this.enqueue();
        }
    }

    /**
     * {@link IAction} that will be passed to {@link IMessageReceiver}'s.
     */
    private class ReceiverCallback implements IAction<Throwable> {
        @Override
        public void execute(final Throwable exception)
                throws ActionExecuteException, InvalidArgumentException {
            if (null != exception) {
                try {
                    handleCompletedExceptionally(exception);
                } catch (NoExceptionHandleChainException e) {
                    throw new ActionExecuteException("Exception occurred while handling exception occurred in message receiver.", e);
                }
            } else {
                if (messageProcessingSequence.next()) {
                    MessageProcessor.this.enqueue();
                }
            }
        }
    }

    /**
     * The constructor.
     *
     * @param taskQueue                    the queue to be executed from
     * @param messageProcessingSequence    a {@link IMessageProcessingSequence} to use
     * @throws InvalidArgumentException if taskQueue is {@code null}
     * @throws InvalidArgumentException if messageProcessingSequence is {@code null}
     */
    public MessageProcessor(final IQueue<ITask> taskQueue, final IMessageProcessingSequence messageProcessingSequence)
            throws InvalidArgumentException {
        if (null == taskQueue) {
            throw new InvalidArgumentException("Task queue should not be null.");
        }

        if (null == messageProcessingSequence) {
            throw new InvalidArgumentException("Message processing sequence should not be null.");
        }

        this.taskQueue = taskQueue;
        this.messageProcessingSequence = messageProcessingSequence;

        this.receiverCallback = new ReceiverCallback();
        this.reEnqueueAction = new ReEnqueueAction();
    }

    /**
     * Process given message in given context.
     *
     * @param theMessage the message to process
     * @param theContext the context to process message in
     * @throws InvalidArgumentException if theMessage is {@code null}
     * @throws InvalidArgumentException if theContext is {@code null}
     */
    public void process(final IMessage theMessage, final IObject theContext)
            throws InvalidArgumentException {
        // TODO: Ensure that there is no process in progress
        this.message = theMessage;
        this.context = theContext;
        messageProcessingSequence.reset();
        enqueue();
    }

    @Override
    public void execute() throws TaskExecutionException {
        try {
            // TODO: Setup context.
            messageProcessingSequence.getCurrentReceiver().receive(message, receiverCallback);
        } catch (Throwable e) {
            try {
                handleCompletedExceptionally(e);
            } catch (final NoExceptionHandleChainException e1) {
                throw new TaskExecutionException("Exception occurred while handling exception occurred in message receiver.", e1);
            }
        }
    }

    private void handleCompletedExceptionally(final Throwable exception)
            throws NoExceptionHandleChainException {
        OutOfResourceException outOfResourceException = getOutOfResourcesCause(exception);

        if (null != outOfResourceException) {
            outOfResourceException.getSource().onAvailable(reEnqueueAction);
            return;
        }

        // TODO: Store exception in message/context
        messageProcessingSequence.catchException(exception);
        enqueue();
    }

    private void enqueue() {
        try {
            taskQueue.put(this);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static OutOfResourceException getOutOfResourcesCause(final Throwable exception) {
        Throwable e;

        for (e = exception; null != e && !(e instanceof OutOfResourceException);) {
            e = e.getCause();
        }

        return (null == e) ? null : ((OutOfResourceException) e);
    }
}
