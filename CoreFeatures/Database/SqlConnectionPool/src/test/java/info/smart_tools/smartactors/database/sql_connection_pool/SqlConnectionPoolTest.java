package info.smart_tools.smartactors.database.sql_connection_pool;

import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.FunctionExecutionException;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.base.interfaces.ipool.exception.PoolPutException;
import info.smart_tools.smartactors.base.interfaces.ipool.exception.PoolTakeException;
import info.smart_tools.smartactors.database.interfaces.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.database.interfaces.istorage_connection.exception.StorageException;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SqlConnectionPoolTest {

    @Test
    public void check_creation_of_connection_pool() {
        IPool pool = new SqlConnectionPool(1, () -> mock(IStorageConnection.class));
        assertNotNull(pool);
        IStorageConnection connection = null;
        try {
            connection = (IStorageConnection) pool.take();
        } catch (PoolTakeException e) { /**/}
        assertNotNull(connection);

        try {
            pool = new SqlConnectionPool(0, () -> mock(IStorageConnection.class));
            fail();
        } catch (IllegalArgumentException e) {/**/}

        try {
            pool = new SqlConnectionPool(1, null);
            fail();
        } catch (IllegalArgumentException e) {/**/}
    }

    @Test
    public void check_creation_new_connections()
            throws Exception {
        IPool pool = new SqlConnectionPool(2, () -> mock(IStorageConnection.class));
        IStorageConnection connection1 = (IStorageConnection) pool.take();
        IStorageConnection connection2 = (IStorageConnection) pool.take();

        assertNotEquals(connection1, connection2);
    }

    @Test
    public void check_reusing_pooled_connections()
            throws Exception {
        Object mutex = new Object();
        AtomicInteger creationCount = new AtomicInteger();
        IPool pool = new SqlConnectionPool(1, () -> {
            creationCount.getAndIncrement();
            IStorageConnection con = mock(IStorageConnection.class);
            try {
                when(con.validate()).thenReturn(true);
            } catch (StorageException e) {
                e.printStackTrace();
            }
            return con;
        });
        IStorageConnection connection = (IStorageConnection) pool.take();
        ConnectionWrapper wrapper = new ConnectionWrapper();
        Thread thread = new Thread(() -> {
            try {
                wrapper.setConnection((IStorageConnection) pool.take());
            } catch (Exception e) {/**/}
            synchronized (mutex) {
                mutex.notify();
            }
        });
        thread.start();
        while (thread.getState() != Thread.State.WAITING) {
            if (!thread.isAlive()) {
                thread.interrupt();
            }
        }
        pool.put(connection);
        synchronized (mutex) {
            mutex.wait();
        }
        assertEquals(connection, wrapper.getConnection());
        assertEquals(creationCount.get(), 1);
    }

    @Test
    public void check_creation_new_connection_on_validation_failed()
            throws Exception {
        AtomicInteger creationCount = new AtomicInteger();
        IPool pool = new SqlConnectionPool(1, () -> {
            IStorageConnection con = mock(IStorageConnection.class);
            creationCount.getAndIncrement();
            try {
                when(con.validate()).thenReturn(false);
            } catch (StorageException e) {
                e.printStackTrace();
            }
            return con;
        });
        IStorageConnection connection = (IStorageConnection) pool.take();
        pool.put(connection);
        IStorageConnection connection1 = (IStorageConnection) pool.take();
        assertNotEquals(connection, connection1);
        assertEquals(creationCount.get(), 2);
    }

    @Test (expected = PoolTakeException.class)
    public void check_pool_take_exception_on_exception_in_creation_function()
            throws Exception {
        IPool pool = new SqlConnectionPool(1, () -> {
            throw new FunctionExecutionException("exception");
        });
        pool.take();
        fail();
    }

    @Test (expected = PoolPutException.class)
    public void check_pool_put_exception_on_put_value_with_not_expected_type()
            throws Exception {
        IPool pool = new SqlConnectionPool(1, () -> mock(IStorageConnection.class));
        pool.take();
        pool.put(1);
        fail();
    }

    @Test
    public void check_pool_on_put_null_value()
            throws Exception {
        IPool pool = new SqlConnectionPool(1, () -> mock(IStorageConnection.class));
        IStorageConnection connection1 = (IStorageConnection) pool.take();
        pool.put(null);
        IStorageConnection connection2 = (IStorageConnection) pool.take();
        assertNotEquals(connection1, connection2);
    }

    @Test
    public void check_task_execution_on_put_if_pool_has_free_connections()
            throws Exception {
        AtomicBoolean checker = new AtomicBoolean(false);
        IPool pool = new SqlConnectionPool(1, () -> mock(IStorageConnection.class));
        IStorageConnection connection1 = (IStorageConnection) pool.take();
        pool.put(connection1);
        pool.onAvailable(() -> checker.set(true));
        assertTrue(checker.get());
    }

    @Test (expected = RuntimeException.class)
    public void check_task_execution_with_exception_on_put_if_pool_has_free_connections()
            throws Exception {
        IPool pool = new SqlConnectionPool(1, () -> mock(IStorageConnection.class));
        IStorageConnection connection1 = (IStorageConnection) pool.take();
        pool.put(connection1);
        pool.onAvailable(() -> {
            throw new ActionExecuteException("exception");
        });
        fail();
    }


    @Test
    public void check_task_execution_on_put_if_pool_has_not_free_connections()
            throws Exception {
        AtomicBoolean checker = new AtomicBoolean(false);
        IPool pool = new SqlConnectionPool(1, () -> mock(IStorageConnection.class));
        IStorageConnection connection1 = (IStorageConnection) pool.take();
        pool.onAvailable(() -> checker.set(true));
        pool.put(connection1);
        assertTrue(checker.get());
    }

    @Test
    public void check_task_execution_with_exception_on_put_if_pool_has_not_free_connections()
            throws Exception {
        AtomicBoolean checker = new AtomicBoolean(false);
        IPool pool = new SqlConnectionPool(1, () -> mock(IStorageConnection.class));
        IStorageConnection connection1 = (IStorageConnection) pool.take();
        pool.onAvailable(() -> {
            checker.set(true);
            throw new ActionExecuteException("exception");
        });
        pool.put(connection1);
        assertTrue(checker.get());
    }
}


class ConnectionWrapper {
    private IStorageConnection connection = null;

    IStorageConnection getConnection() {
        return connection;
    }

    void setConnection(IStorageConnection connection) {
        this.connection = connection;
    }
}
