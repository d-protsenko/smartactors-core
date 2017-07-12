package info.smart_tools.smartactors.database_postgresql_async_ops_collection.async_ops_collection_actor;

import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.database.async_operation_collection.AsyncOperationCollection;
import info.smart_tools.smartactors.database.async_operation_collection.IAsyncOperationCollection;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.database_postgresql_async_ops_collection.async_ops_collection_actor.wrapper.AsyncOpsWrapper;
import info.smart_tools.smartactors.feature_loading_system.bootstrap.Bootstrap;
import info.smart_tools.smartactors.field_plugins.ifield_plugin.IFieldPlugin;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.ioc_plugins.ioc_simple_container_plugin.PluginIOCSimpleContainer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AsyncOpsCollectionActorTest {

    private AsyncOpsCollectionActor actor;
    private AsyncOpsWrapper wrapper;

    @Before
    public void before() throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        new PluginIOCSimpleContainer(bootstrap).load();
        new PluginIOCKeys(bootstrap).load();
        new IFieldNamePlugin(bootstrap).load();
        new IFieldPlugin(bootstrap).load();
        new PluginDSObject(bootstrap).load();
        bootstrap.start();

        IOC.register(Keys.getOrAdd("TestPostgresConnectionOptions"), new SingletonStrategy(mock(ConnectionOptions.class)));
        IOC.register(Keys.getOrAdd("PostgresConnectionPool"), new SingletonStrategy(mock(IPool.class)));

        actor = new AsyncOpsCollectionActor();
        wrapper = mock(AsyncOpsWrapper.class);
        when(wrapper.getConnectionOptionsRegistrationName()).thenReturn("TestPostgresConnectionOptions");
        when(wrapper.getCollectionName()).thenReturn("test_database");
    }

    @Test
    public void Should_RegisterAsyncCollection() throws Exception {
        actor.register(wrapper);
        Object o = IOC.resolve(Keys.getOrAdd(IAsyncOperationCollection.class.getCanonicalName()));
        assertTrue(o instanceof AsyncOperationCollection);
    }
}
