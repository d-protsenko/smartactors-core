package info.smart_tools.smartactors.scheduler.actor.impl.remote_storage;

import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.base.pool_guard.IPoolGuard;
import info.smart_tools.smartactors.base.pool_guard.PoolGuard;
import info.smart_tools.smartactors.database_in_memory_plugins.in_memory_database_plugin.PluginInMemoryDatabase;
import info.smart_tools.smartactors.database_in_memory_plugins.in_memory_db_tasks_plugin.PluginInMemoryDBTasks;
import info.smart_tools.smartactors.field_plugins.ifield_plugin.IFieldPlugin;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.EntryNotFoundException;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link DatabaseRemoteStorage}.
 */
public class DatabaseRemoteStorageTest extends PluginsLoadingTestBase {
    private IPool connectionPool;
    private ISchedulerEntry[] entries;

    @Override
    protected void loadPlugins() throws Exception {
        load(ScopedIOCPlugin.class);
        load(PluginScopeProvider.class);
        load(PluginIOCKeys.class);
        load(PluginDSObject.class);
        load(IFieldNamePlugin.class);
        load(IFieldPlugin.class);
        load(PluginInMemoryDatabase.class);
        load(PluginInMemoryDBTasks.class);
    }

    @Override
    protected void registerMocks() throws Exception {
        connectionPool = mock(IPool.class);

        try (IPoolGuard guard = new PoolGuard(connectionPool)) {
            ITask createTask = IOC.resolve(
                    Keys.getKeyByName("db.collection.create"),
                    guard.getObject(),
                    "scheduler_collection",
                    IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject")));

            createTask.execute();
        }

        entries = new ISchedulerEntry[11];

        for (int i = 0; i < entries.length; i++) {
            entries[i] = mock(ISchedulerEntry.class);
            when(entries[i].getLastTime()).thenReturn(100L * i);
            when(entries[i].getState()).thenReturn(IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"),
                    String.format(("{" +
                            "'entryId':'entry-%010d'" +
                            "}").replace('\'', '"'), i)));
        }
    }

    private void assertSameEntries(List<ISchedulerEntry> expect, List<IObject> dl) throws Exception {
        assertEquals(expect.size(), dl.size());

        for (int i = 0; i < expect.size(); i++) {
            assertEquals(expect.get(i).getState().<String>serialize(), dl.get(i).<String>serialize());
        }
    }

//    @Test
    public void Should_downloadEntries()
            throws Exception {
        IRemoteEntryStorage remoteEntryStorage = new DatabaseRemoteStorage(connectionPool, "scheduler_collection");

        for (ISchedulerEntry e : entries) remoteEntryStorage.saveEntry(e);

        assertSameEntries(Arrays.asList(entries), remoteEntryStorage.downloadEntries(10000, null, 100));

        assertSameEntries(Arrays.asList(Arrays.copyOf(entries, 6)), remoteEntryStorage.downloadEntries(550, null, 100));

        assertSameEntries(Arrays.asList(Arrays.copyOf(entries, 5)), remoteEntryStorage.downloadEntries(650, null, 5));
        assertSameEntries(Arrays.asList(Arrays.copyOfRange(entries, 5, 7)), remoteEntryStorage.downloadEntries(650, entries[9].getState(), 5));
    }

    @Test(expected = EntryNotFoundException.class)
    public void Should_throwWhenSingleRequiredEntryIsNotFound()
            throws Exception {
        IRemoteEntryStorage remoteEntryStorage = new DatabaseRemoteStorage(connectionPool, "scheduler_collection");
        remoteEntryStorage.querySingleEntry("not-exist-id");
    }
}
