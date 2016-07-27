package info.smart_tools.smartactors.core.examples;

import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.ipool.exception.PoolTakeException;
import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.core.istorage_connection.exception.StorageException;
import org.junit.Before;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A set of examples to work with DB collections.
 */
public class DBCollectionExample {

    IPool pool;
    IStorageConnection connection;

    @Before
    public void setUp() throws PoolTakeException, StorageException {
        connection = mock(IStorageConnection.class);
        when(connection.validate()).thenReturn(true);

        pool = mock(IPool.class);
        when(pool.take()).thenReturn(connection);
    }

}
