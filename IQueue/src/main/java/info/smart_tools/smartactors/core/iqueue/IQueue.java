package info.smart_tools.smartactors.core.iqueue;

/**
 * Blocking queue.
 *
 * @param <T> element type.
 */
public interface IQueue <T> {
    /**
     * Put the item to the tail of this queue.
     *
     * @param item the item to put into queue
     * @throws InterruptedException if thread is interrupted
     */
    void put(T item) throws InterruptedException;

    /**
     * Take the element from the head of the queue blocking if the queue is empty.
     *
     * @return the element from head of queue
     * @throws InterruptedException if thread is interrupted
     */
    T take() throws InterruptedException;

    /**
     * Take the element from the head of the queue returning {@code null} if the queue is empty.
     *
     * @return the element from head of queue or {@code null} if queue is empty
     */
    T tryTake();
}
