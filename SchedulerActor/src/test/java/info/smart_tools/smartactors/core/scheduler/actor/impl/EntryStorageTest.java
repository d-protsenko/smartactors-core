package info.smart_tools.smartactors.core.scheduler.actor.impl;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.base.pool_guard.IPoolGuard;
import info.smart_tools.smartactors.base.pool_guard.PoolGuard;
import info.smart_tools.smartactors.core.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.core.scheduler.interfaces.exceptions.EntryStorageAccessException;
import info.smart_tools.smartactors.database_in_memory_plugins.in_memory_database_plugin.PluginInMemoryDatabase;
import info.smart_tools.smartactors.database_in_memory_plugins.in_memory_db_tasks_plugin.PluginInMemoryDBTasks;
import info.smart_tools.smartactors.field_plugins.ifield_plugin.IFieldPlugin;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Test for {@link EntryStorage}.
 */
public class EntryStorageTest extends PluginsLoadingTestBase {
    private IPool connectionPool;
    private IResolveDependencyStrategy restoreEntryStrategy;
    private IObject[] saved;
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
//        load(NullConnectionPoolPlugin.class);
    }

    @Override
    protected void registerMocks() throws Exception {
//        connectionPool = IOC.resolve(Keys.getOrAdd("DatabaseConnectionPool"));
        connectionPool = mock(IPool.class);

        try (IPoolGuard guard = new PoolGuard(connectionPool)) {
            ITask createTask = IOC.resolve(
                    Keys.getOrAdd("db.collection.create"),
                    guard.getObject(),
                    "scheduler_collection",
                    IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName())));

            createTask.execute();

            saved = new IObject[] {
                    IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                            "{'strategy':'strategy1','entryId':'1'}".replace('\'', '"')),
                    IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                            "{'strategy':'strategy2','entryId':'2'}".replace('\'', '"')),
                    IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                            "{'strategy':'strategy3','entryId':'3'}".replace('\'', '"')),
                    IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                            "{'strategy':'strategy4','entryId':'4'}".replace('\'', '"')),
                    IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                            "{'strategy':'strategy5','entryId':'5'}".replace('\'', '"')),
                    IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                            "{'strategy':'strategy6','entryId':'6'}".replace('\'', '"')),
                    IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                            "{'strategy':'strategy7','entryId':'7'}".replace('\'', '"')),
            };

            entries = new ISchedulerEntry[saved.length + 2];

            for (int i = 0; i < entries.length; i++) {
                entries[i] = mock(ISchedulerEntry.class);
                when(entries[i].getId()).thenReturn(String.valueOf(i));
                when(entries[i].getState()).thenReturn(i < saved.length ? saved[i] : mock(IObject.class));
            }

            for (IObject obj : saved) {
                ITask task = IOC.resolve(
                        Keys.getOrAdd("db.collection.insert"),
                        guard.getObject(),
                        "scheduler_collection",
                        obj);

                task.execute();
            }
        }

        restoreEntryStrategy = mock(IResolveDependencyStrategy.class);
        when(restoreEntryStrategy.resolve(any(), any())).thenReturn(entries[0], Arrays.copyOfRange(entries, 1, entries.length));
        IOC.register(Keys.getOrAdd("restore scheduler entry"), restoreEntryStrategy);
    }

    private int countDBEntries() throws Exception {
        int[] res = new int[] {-1};

        try (IPoolGuard guard = new PoolGuard(connectionPool)) {
            ITask createTask = IOC.resolve(
                    Keys.getOrAdd("db.collection.count"),
                    guard.getObject(),
                    "scheduler_collection",
                    IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()), "{'filter':{},'sort':[]}".replace('\'','"')),
                    (IAction<Long>) c -> res[0] = c.intValue());

            createTask.execute();
        }

        return res[0];
    }

    @Test
    public void Should_downloadAndRestoreEntries()
            throws Exception {
        EntryStorage storage = new EntryStorage(connectionPool, "scheduler_collection");

        assertFalse(storage.downloadNextPage(2));
        assertFalse(storage.downloadNextPage(2));
        assertFalse(storage.downloadNextPage(2));
        assertTrue(storage.downloadNextPage(2));
        assertTrue(storage.downloadNextPage(2));

        verify(restoreEntryStrategy, times(saved.length)).resolve(any(), same(storage));
    }

    @Test
    public void Should_locallyStoreEntries()
            throws Exception {
        EntryStorage storage = new EntryStorage(connectionPool, "scheduler_collection");

        storage.saveLocally(entries[0]);
        storage.saveLocally(entries[1]);
        storage.saveLocally(entries[2]);

        assertSame(entries[1], storage.getEntry("1"));
    }

    @Test
    public void Should_cancelOldEntryWhenItIsOverriddenByNewOne()
            throws Exception {
        when(entries[1].getId()).thenReturn("0");

        EntryStorage storage = new EntryStorage(connectionPool, "scheduler_collection");

        storage.saveLocally(entries[0]);
        storage.saveLocally(entries[1]);

        verify(entries[0]).cancel();
        assertSame(entries[1], storage.getEntry("0"));
    }

    @Test
    public void Should_saveEntryInDatabase()
            throws Exception {
        int iCnt = countDBEntries();

        EntryStorage storage = new EntryStorage(connectionPool, "scheduler_collection");

        storage.save(entries[saved.length]);

        assertEquals(1, countDBEntries() - iCnt);
    }

    @Test
    public void Should_deleteEntryFromBothDBAndMemory()
            throws Exception {
        int iCnt = countDBEntries();

        EntryStorage storage = new EntryStorage(connectionPool, "scheduler_collection");

        assertTrue(storage.downloadNextPage(100));
        storage.saveLocally(entries[1]);

        assertEquals(entries[1], storage.getEntry("1"));

        storage.delete(entries[1]);

        try {
            storage.getEntry("1");
            fail();
        } catch (EntryStorageAccessException e) {}

        assertEquals(-1, countDBEntries()-iCnt);
    }

    @Test
    public void Should_enumerateAllEntries()
            throws Exception {
        EntryStorage storage = new EntryStorage(connectionPool, "scheduler_collection");

        storage.saveLocally(entries[1]);
        storage.saveLocally(entries[2]);

        assertEquals(new HashSet<>(Arrays.asList(entries[1], entries[2])), new HashSet<>(storage.listLocalEntries()));
    }
}
