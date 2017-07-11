package info.smart_tools.smartactors.database_postgresql_async_ops_collection.async_ops_collection_plugin;

import info.smart_tools.smartactors.database_postgresql_async_ops_collection.async_ops_collection_actor.AsyncOpsCollectionActor;
import info.smart_tools.smartactors.feature_loading_system.bootstrap.Bootstrap;
import info.smart_tools.smartactors.field_plugins.ifield_plugin.IFieldPlugin;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.ioc_plugins.ioc_simple_container_plugin.PluginIOCSimpleContainer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AsyncOpsCollectionPluginTest {

    @Before
    public void init() throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        new PluginIOCSimpleContainer(bootstrap).load();
        new PluginIOCKeys(bootstrap).load();
        new IFieldNamePlugin(bootstrap).load();
        new IFieldPlugin(bootstrap).load();

        new AsyncOpsCollectionPlugin(bootstrap).load();
        bootstrap.start();
    }

    @Test
    public void Should_registerActor() throws ResolutionException {
        assertTrue(IOC.resolve(Keys.getOrAdd("PostgresAsyncOpsCollectionActor"))
                instanceof AsyncOpsCollectionActor);
    }

}
