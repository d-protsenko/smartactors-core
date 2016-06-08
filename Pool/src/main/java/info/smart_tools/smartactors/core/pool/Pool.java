package info.smart_tools.smartactors.core.pool;

import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.ipool.exception.PoolException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;


/**
 * Implementation of {@link info.smart_tools.smartactors.core.ipool.IPool}
 */
public class Pool implements IPool {
    private final ArrayBlockingQueue<Object> freeItems;
    private AtomicInteger freeItemsCounter = new AtomicInteger();

    /**
     * Local function for creation new instances of items
     */
    private Supplier<Object> creationFunction;

    /**
     * Constructs new Scope with defined parent scope
     * @param maxItems the maximum of active items.
     * @param func the function for creating new instances of items
     */
    public Pool(final Integer maxItems, final Supplier<Object>  func) {
        this.freeItems = new ArrayBlockingQueue<>(maxItems);
        this.freeItemsCounter.set(maxItems);
        if (func == null) {
            throw new IllegalArgumentException("Incoming argument should not be null.");
        }
        this.creationFunction = func;
    }

    /**
     * Get a value by the key from the scope.
     * @throws PoolException if error was occurred
     */
    public Object tryTake() throws PoolException {
        if (freeItemsCounter.getAndDecrement() <= 0) {
            freeItemsCounter.incrementAndGet();
            return null;
        }

        if (freeItems.isEmpty()) {
            return creationFunction.get();
        }

        return freeItems.remove();
    }

    /**
     * Stores a value to the pool.
     * @param item the item that now free.
     * @throws PoolException if any error occurred
     */
    public void put(final Object item) throws PoolException {
        freeItems.add(item);
        freeItemsCounter.getAndIncrement();
    }
}
