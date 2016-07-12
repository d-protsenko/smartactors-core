package info.smart_tools.smartactors.core.actor_receiver;

import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.message_processing.exceptions.MessageReceiveException;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link IMessageReceiver} that performs all necessary synchronization for a nested receiver to be executed as actor.
 *
 * {@link ActorReceiver} provides synchronization only for synchronous operation of a child receiver i.e. if child
 * receiver starts an asynchronous operation (by not calling passed callback) it may be called again by {@link
 * ActorReceiver} just after {@link IMessageReceiver#receive} method of child returns.
 */
public class ActorReceiver implements IMessageReceiver {
    // It's preferred to use ConcurrentLinkedQueue that uses lock-free algorithms
    private final Queue<Object[]> queue;

    // Atomic flag. True if any message is being processed by childReceiver
    private final AtomicBoolean isBusy;

    private final IMessageReceiver childReceiver;

    /**
     * The constructor.
     *
     * @param childReceiver    the child receiver
     * @throws InvalidArgumentException if childReceiver is {@code null}.
     * @throws ResolutionException if resolution of any dependencies fails.
     */
    public ActorReceiver(final IMessageReceiver childReceiver)
            throws InvalidArgumentException, ResolutionException {
        if (null == childReceiver) {
            throw new InvalidArgumentException("Child receiver should not be null.");
        }

        this.childReceiver = childReceiver;

        this.queue = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), "actor_receiver_queue"));
        this.isBusy = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), "actor_receiver_busyness_flag"));
    }

    @Override
    public void receive(final IMessageProcessor processor, final IObject arguments, final IAction<Throwable> onEnd)
            throws MessageReceiveException {
        if (isBusy.compareAndSet(false, true)) {
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
            if (isBusy.compareAndSet(false, true)) {
                Object[] arr;

                try {
                    while (null != (arr = queue.poll())) {
                        executeOne(arr);
                    }
                } finally {
                    isBusy.set(false);
                }
            } else {
                break;
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
