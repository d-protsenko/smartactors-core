package info.smart_tools.smartactors.database.sql_connection_pool;

import info.smart_tools.smartactors.base.interfaces.iaction.IFunction0;
import info.smart_tools.smartactors.base.interfaces.iaction.IPoorAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.FunctionExecutionException;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.base.interfaces.ipool.exception.PoolPutException;
import info.smart_tools.smartactors.base.interfaces.ipool.exception.PoolTakeException;
import info.smart_tools.smartactors.database.interfaces.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.database.interfaces.istorage_connection.exception.StorageException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation of {@link IPool} for sharing sql connections
 */
public class SqlConnectionPool implements IPool {

    private final BlockingQueue<IStorageConnection> items;
    private final ReentrantLock lock = new ReentrantLock();

    private Integer size;
    private Integer itemCounter = 0;
    private ConcurrentLinkedQueue<IPoorAction> taskQueue = new ConcurrentLinkedQueue<>();
    /**
     * Local function for creation new instances of items
     */
    private IFunction0<IStorageConnection> creationFunction;

    /**
     * Constructs new Sql connection pool
     * @param size the size of pool
     * @param func the function for creating new instances of items
     */
    public SqlConnectionPool(final Integer size, final IFunction0<IStorageConnection> func) {
        if (func == null) {
            throw new IllegalArgumentException("Function must be not null");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Pool size mast be more 0");
        }

        this.size = size;
        this.items = new ArrayBlockingQueue<>(size);
        this.creationFunction = func;
    }

    @Override
    public Object take() throws PoolTakeException {
        IStorageConnection connection = items.poll();
        if (null == connection) {
            if (!lock.isLocked()) {
                if (lock.tryLock()) {
                    try {
                        ++itemCounter;
                        return createNewConnection();
                    } finally {
                        if (itemCounter < size) {
                            lock.unlock();
                        }
                    }
                }
            } else {
                try {
                    connection = items.take();
                } catch (InterruptedException e) {
                    throw new PoolTakeException("Interrupted while waiting for pool object.", e);
                }
            }
        }
        boolean isValid = false;
        try {
            isValid = connection.validate();
        } catch (StorageException e) {/**/}
        if (!isValid) {
            try {
                connection.close();
            } catch (StorageException e) {/**/}
            connection = createNewConnection();
        }

        return connection;
    }

    @Override
    public void put(final Object item) throws PoolPutException {
        IStorageConnection connection;
        try {
            connection = (IStorageConnection) item;
        } catch (ClassCastException e) {
            throw new PoolPutException(
                "Unexpected type of object. Expected - IStorageConnection, but actual - " + item.getClass().toString(),
                e
            );
        }
        try {
            items.add(connection);
        } catch (Exception e) {
            if (itemCounter >= size) {
                --itemCounter;
                if (lock.isLocked()) {
                    lock.unlock();
                }
            }
        }
        IPoorAction task = taskQueue.poll();
        if (task != null) {
            try {
                task.execute();
            } catch (ActionExecuteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAvailable(final IPoorAction action) {
        try {
            if (items.size() > 0) {
                action.execute();
                return;
            }
            this.taskQueue.add(action);
        } catch (ActionExecuteException e) {
            throw new RuntimeException("Failed to execute PoorAction", e);
        }
    }

    private IStorageConnection createNewConnection()
            throws PoolTakeException {
        try {
            return this.creationFunction.execute();
        } catch (FunctionExecutionException e) {
            throw new PoolTakeException("Pool object creation failed.", e);
        }
    }
}
