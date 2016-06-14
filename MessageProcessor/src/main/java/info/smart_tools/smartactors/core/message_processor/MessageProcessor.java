package info.smart_tools.smartactors.core.message_processor;

import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iaction.IPoorAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.imessage.IMessage;
import info.smart_tools.smartactors.core.imessage_receiver.IMessageReceiver;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.iresource_source.exceptions.OutOfResourceException;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;

/**
 *
 */
public class MessageProcessor implements ITask {
    private IObject context;
    private IMessage message;

    private final IQueue<ITask> taskQueue;

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
                handleCompletedExceptionally(exception);
            } else {
                if (nextReceiver()) {
                    MessageProcessor.this.enqueue();
                }
            }
        }
    }

    /**
     * @param taskQueue the queue to be executed from
     */
    public MessageProcessor(final IQueue<ITask> taskQueue) {
        this.taskQueue = taskQueue;

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
        resetSequence();
        enqueue();
    }

    @Override
    public void execute() throws TaskExecutionException {
        try {
            // TODO: Setup context.
            getCurrentReceiver().receive(message, receiverCallback);
        } catch (Throwable e) {
            handleCompletedExceptionally(e);
        }
    }

    private void handleCompletedExceptionally(final Throwable exception) {
        OutOfResourceException outOfResourceException = getOutOfResourcesCause(exception);

        if (null != outOfResourceException) {
            outOfResourceException.getSource().onAvailable(reEnqueueAction);
            return;
        }

        // TODO: Handle operation completed exceptionally (but not because of resource unavailability)
    }

    private void enqueue() {
        try {
            taskQueue.put(this);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void resetSequence() {
        // TODO: Implement
    }

    private IMessageReceiver getCurrentReceiver() {
        // TODO: Implement
        return null;
    }

    /**
     * Move to next message receiver.
     *
     * @return {@code true} if there is a receiver, {@code false} if there is no more receivers
     */
    private boolean nextReceiver() {
        // TODO: Implement
        return true;
    }

    private static OutOfResourceException getOutOfResourcesCause(final Throwable exception) {
        Throwable e;

        for (e = exception; null != e && !(e instanceof OutOfResourceException);) {
            e = e.getCause();
        }

        return (null == e) ? null : ((OutOfResourceException) e);
    }
}
