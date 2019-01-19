package info.smart_tools.smartactors.base.pool;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IPoorAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecutionException;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.base.interfaces.ipool.exception.PoolPutException;
import info.smart_tools.smartactors.base.interfaces.ipool.exception.PoolTakeException;
import info.smart_tools.smartactors.base.interfaces.iresource_source.exceptions.OutOfResourceException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;


/**
 * Implementation of {@link IPool}
 */
public class Pool implements IPool {
    private Integer maxItemsCount;
    private final ArrayBlockingQueue<Object> freeItems;
    private AtomicInteger freeItemsCounter = new AtomicInteger();
    private ConcurrentLinkedQueue<IPoorAction> taskQueue = new ConcurrentLinkedQueue<>();

    /**
     * Local function for creation new instances of items
     */
    private Supplier<Object> creationFunction;

    /**
     * Constructs new Pool
     * @param maxItems the maximum of active items.
     * @param func the function for creating new instances of items
     */
    public Pool(final Integer maxItems, final Supplier<Object>  func) {
        if (func == null) {
            throw new IllegalArgumentException("Function must be not null");
        }
        if (maxItems <= 0) {
            throw new IllegalArgumentException("Count of max items mast be more 0");
        }
        this.maxItemsCount = maxItems;
        this.freeItems = new ArrayBlockingQueue<>(maxItems);
        this.freeItemsCounter.set(maxItems);
        this.creationFunction = func;
    }

    /**
     * Get a value by the key from the scope.
     * @throws PoolTakeException if error was occurred
     * @return Object from pool
     */
    public Object take() throws PoolTakeException {
        if (freeItemsCounter.getAndDecrement() <= 0) {
            freeItemsCounter.getAndIncrement();
            try {
                throw new PoolTakeException("Reached limit of items for this pool.", new OutOfResourceException(this));
            } catch (InvalidArgumentException ex) {
                throw new PoolTakeException("Reached limit of items for this pool.", ex);
            }
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
            if (maxItemsCount >= freeItemsCounter.get()) {
                freeItems.add(item);
                freeItemsCounter.getAndIncrement();

                IPoorAction task = taskQueue.poll();
                if (task != null) {
                    task.execute();
                }
            }
        } catch (Exception e) {
            throw new PoolPutException("Error was occurred", e);
        }
    }

    /**
     * Add action for executing when the resource becomes available
     * @param action action to execute when the resource becomes available
     */
    public void onAvailable(final IPoorAction action) {
        try {
            if (freeItemsCounter.get() > 0) {
                action.execute();
                return;
            }
            this.taskQueue.add(action);
        } catch (ActionExecutionException e) {
            throw new RuntimeException("Failed to execute PoorAction", e);
        }
    }
}
