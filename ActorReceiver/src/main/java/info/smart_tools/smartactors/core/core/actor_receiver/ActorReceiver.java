package info.smart_tools.smartactors.core.core.actor_receiver;

import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.message_processing.exceptions.MessageReceiveException;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class ActorReceiver implements IMessageReceiver {
    // ConcurrentLinkedQueue uses lock-free algorithms
    private final ConcurrentLinkedQueue<Object[]> queue;

    // Atomic flag. True if any message is being processed by childReceiver
    private final AtomicBoolean isBusy;

    private final IMessageReceiver childReceiver;

    /**
     * The constructor.
     *
     * @param childReceiver    the child receiver
     * @throws InvalidArgumentException if childReceiver is {@code null}.
     */
    public ActorReceiver(final IMessageReceiver childReceiver)
            throws InvalidArgumentException {
        this.childReceiver = childReceiver;

        this.queue = new ConcurrentLinkedQueue<>();
        this.isBusy = new AtomicBoolean(false);
    }

    @Override
    public void receive(final IMessageProcessor processor, final IObject arguments, final IAction<Throwable> onEnd)
            throws MessageReceiveException {
        if (!isBusy.getAndSet(true)) {
            try {
                childReceiver.receive(processor, arguments, onEnd);
            } catch (Throwable e) {
                try {
                    onEnd.execute(e);
                } catch (Throwable ee) {
                    ee.addSuppressed(e);
                    catchCriticalException(ee);
                }
            } finally {
                isBusy.set(false);
            }
        } else {
            queue.add(new Object[]{processor, arguments, onEnd});
        }

        while (!queue.isEmpty()) {
            if (!isBusy.getAndSet(true)) {
                Object[] arr;

                try {
                    while (null != (arr = queue.poll())) {
                        executeOne(arr);
                    }
                } finally {
                    isBusy.set(false);
                }
            }
        }
    }

    private void executeOne(final Object[] arr) {
        final IMessageProcessor messageProcessor = (IMessageProcessor) arr[0];
        final IObject arguments = (IObject) arr[1];
        final IAction callback = (IAction) arr[2];

        try {
            childReceiver.receive(messageProcessor, arguments, callback);
        } catch (Throwable e) {
            try {
                callback.execute(e);
            } catch (Throwable ee) {
                ee.addSuppressed(e);
                catchCriticalException(ee);
            }
        }
    }

    /**
     * Handler for exceptions occurred in callbacks passed to receiver. Such exceptions (in some cases) can not be
     * rethrown because of the {@link #receive} call being completed.
     *
     * @param e    the exception
     */
    private void catchCriticalException(final Throwable e) {
        //
    }
}
