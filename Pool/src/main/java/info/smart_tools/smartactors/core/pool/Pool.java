package info.smart_tools.smartactors.core.pool;

import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.ipool.exception.PoolException;
import info.smart_tools.smartactors.core.ipool.exception.PoolPutException;
import info.smart_tools.smartactors.core.ipool.exception.PoolTakeException;

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
     * @throws PoolTakeException if error was occurred
     */
    public Object take() throws PoolTakeException {
        if (freeItemsCounter.getAndDecrement() <= 0) {
            freeItemsCounter.incrementAndGet();
            return null;
        }

        try {
            Object result = freeItems.poll();
            if (result == null) {
                result = creationFunction.get();
            }

            return result;
        } catch (Exception e) {
            freeItemsCounter.getAndIncrement();
            throw new PoolTakeException("Failed to get item", e);
        }
    }

    /**
     * Stores a value to the pool.
     * @param item the item that now free.
     * @throws PoolPutException if any error occurred
     */
    public void put(final Object item) throws PoolPutException {
        try {
            freeItems.add(item);
            freeItemsCounter.getAndIncrement();
        } catch (Exception e) {
            throw new PoolPutException("Error was occurred", e);
        }
    }
}
