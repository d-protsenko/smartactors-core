package info.smart_tools.smartactors.core.blocking_queue;

import info.smart_tools.smartactors.core.iqueue.IQueue;

/**
 * Implementation of {@link IQueue} that is a facade for standard java queue.
 *
 * @param <T> type of element.
 */
public class BlockingQueue <T> implements IQueue <T> {
    private final java.util.concurrent.BlockingQueue<T> queue;

    /**
     * The constructor.
     *
     * @param queue the underlying standard queue
     */
    public BlockingQueue(final java.util.concurrent.BlockingQueue<T> queue) {
        this.queue = queue;
    }

    @Override
    public void put(final T item) throws InterruptedException {
        queue.put(item);
    }

    @Override
    public T take() throws InterruptedException {
        return queue.take();
    }

    @Override
    public T tryTake() {
        return queue.poll();
    }
}
