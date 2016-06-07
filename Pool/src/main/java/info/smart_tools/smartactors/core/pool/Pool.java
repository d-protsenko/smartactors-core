package info.smart_tools.smartactors.core.pool;

import info.smart_tools.smartactors.core.pool.exception.PoolException;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;


/**
 * Pool
 */
public class Pool {
    private final Set<Object> freeItems = new HashSet<>();
    private final Integer maxFreeItems;

    /**
     * Local function for creation new instances of items
     */
    private Supplier<Object> creationFunction;

    /**
     * Constructs new Scope with defined parent scope
     * @param maxItems the maximum of active items.
     * @param func the function for creating new instances of items
     */
    public Pool(final Integer maxItems, final Supplier<Object> func) {
        this.maxFreeItems = maxItems;
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
        Object result = null;
        synchronized (freeItems) {
            for (Object item : freeItems) {
                freeItems.remove(item);
                result = item;
            }
        }

        if (result == null) {
            result =  creationFunction.get();
        }

        return result;
    }

    /**
     * Stores a value to the pool.
     * @param item the item that now free.
     * @throws PoolException if any error occured
     */
    public void put(final Object item) throws PoolException {
        synchronized (freeItems) {
            if (freeItems.size() < maxFreeItems) {
                freeItems.add(item);
            }
        }
    }
}
