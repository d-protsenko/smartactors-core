package info.smart_tools.smartactors.plugin.null_connection_pool;

import info.smart_tools.smartactors.core.bootstrap.Bootstrap;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.base.pool_guard.PoolGuard;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.plugin.ioc_keys.PluginIOCKeys;
import info.smart_tools.smartactors.plugin.ioc_simple_container.PluginIOCSimpleContainer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class NullConnectionPoolPluginTest {

    @Before
    public void setUp() throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        new PluginIOCSimpleContainer(bootstrap).load();
        new PluginIOCKeys(bootstrap).load();
        new NullConnectionPoolPlugin(bootstrap).load();
        bootstrap.start();
    }

    @Test
    public void testPoolRegistered() throws Exception {
        assertTrue(IOC.resolve(Keys.getOrAdd("DatabaseConnectionPool")) instanceof IPool);
    }

    @Test
    public void testPoolRegisteredWithOptions() throws Exception {
        ConnectionOptions options = mock(ConnectionOptions.class);
        assertTrue(IOC.resolve(Keys.getOrAdd("DatabaseConnectionPool"), options) instanceof IPool);
    }

    @Test
    public void testPoolGuarded() throws Exception {
        IPool pool = IOC.resolve(Keys.getOrAdd("DatabaseConnectionPool"));
        try (PoolGuard guard = new PoolGuard(pool)) {
            assertTrue(guard.getObject() instanceof IStorageConnection);
        }
    }

}
