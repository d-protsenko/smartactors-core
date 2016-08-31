package info.smart_tools.smartactors.plugin.postgres_connection_pool;

import info.smart_tools.smartactors.core.bootstrap.Bootstrap;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.pool.Pool;
import info.smart_tools.smartactors.core.postgres_connection.PostgresConnection;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.plugin.ioc_keys.PluginIOCKeys;
import info.smart_tools.smartactors.plugin.ioc_simple_container.PluginIOCSimpleContainer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class PostgresConnectionPoolPluginTest {

    private ConnectionOptions connectionOptions;

    @Before
    public void setUp() throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        new PluginIOCSimpleContainer(bootstrap).load();
        new PluginIOCKeys(bootstrap).load();
        new PostgresConnectionPoolPlugin(bootstrap).load();
        bootstrap.start();

        connectionOptions = mock(ConnectionOptions.class);
        when(connectionOptions.getMaxConnections()).thenReturn(10);
    }

    @Test
    public void testPoolResolve() throws Exception {
        IPool pool = IOC.resolve(Keys.getOrAdd("PostgresConnectionPool"), connectionOptions);
        assertThat(pool, is(instanceOf(Pool.class)));
    }

    @Test
    public void testPoolResolveDatabaseConnection() throws Exception {
        IPool pool = IOC.resolve(Keys.getOrAdd("DatabaseConnectionPool"), connectionOptions);
        assertThat(pool, is(instanceOf(Pool.class)));
    }

    @Test
    @Ignore("Requires actual DB connection, need to mock JDBC :(")
    public void testPoolReturnConnection() throws Exception {
        IPool pool = IOC.resolve(Keys.getOrAdd("DatabaseConnectionPool"), connectionOptions);
        Object connection = pool.take();
        assertThat(connection, is(instanceOf(PostgresConnection.class)));
    }

    @Test
    public void testResolveSamePool() throws ResolutionException {
        IPool pool1 = IOC.resolve(Keys.getOrAdd("DatabaseConnectionPool"), connectionOptions);
        IPool pool2 = IOC.resolve(Keys.getOrAdd("DatabaseConnectionPool"), connectionOptions);
        assertSame(pool1, pool2);
    }

    @Test
    public void testResolveOtherPool() throws ResolutionException, ReadValueException {
        IPool pool1 = IOC.resolve(Keys.getOrAdd("DatabaseConnectionPool"), connectionOptions);
        ConnectionOptions connectionOptions2 = mock(ConnectionOptions.class);
        when(connectionOptions2.getMaxConnections()).thenReturn(42);
        IPool pool2 = IOC.resolve(Keys.getOrAdd("DatabaseConnectionPool"), connectionOptions2);
        assertNotSame(pool1, pool2);
    }

}
