package info.smart_tools.smartactors.core.scheduler.actor;

import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.scheduler.actor.wrappers.AddEntryQueryMessage;
import info.smart_tools.smartactors.core.scheduler.actor.wrappers.DeleteEntryQueryMessage;
import info.smart_tools.smartactors.core.scheduler.actor.wrappers.ListEntriesQueryMessage;
import info.smart_tools.smartactors.core.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.core.scheduler.interfaces.ISchedulerEntryStorage;
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
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Text for {@link SchedulerActor}.
 */
public class SchedulerActorTest extends PluginsLoadingTestBase {
    private IPool pool;
    private ISchedulerEntryStorage storage;
    private IObject args;
    private AddEntryQueryMessage addEntryQueryMessage;
    private ListEntriesQueryMessage listEntriesQueryMessage;
    private DeleteEntryQueryMessage deleteEntryQueryMessage;
    private IResolveDependencyStrategy newEntryStrategy;
    private ISchedulerEntry entryMock;

    @Override
    protected void loadPlugins() throws Exception {
        load(ScopedIOCPlugin.class);
        load(PluginScopeProvider.class);
        load(PluginIOCKeys.class);
        load(PluginDSObject.class);
        load(IFieldNamePlugin.class);
        load(IFieldPlugin.class);
    }

    @Override
    protected void registerMocks() throws Exception {
        Object connectionOptions = new Object();
        pool = mock(IPool.class);
        storage = mock(ISchedulerEntryStorage.class);
        IResolveDependencyStrategy poolStrategy = mock(IResolveDependencyStrategy.class);
        IResolveDependencyStrategy storageStrategy = mock(IResolveDependencyStrategy.class);

        when(poolStrategy.resolve(same(connectionOptions))).thenReturn(pool);
        when(storageStrategy.resolve(same(pool), eq("the_collection_name")))
                .thenReturn(storage)
                .thenThrow(ResolveDependencyStrategyException.class);
        when(storage.downloadNextPage(anyInt())).thenReturn(true);

        IOC.register(Keys.getOrAdd("the connection options dependency"), new SingletonStrategy(connectionOptions));
        IOC.register(Keys.getOrAdd("the connection pool dependency"), poolStrategy);
        IOC.register(Keys.getOrAdd(ISchedulerEntryStorage.class.getCanonicalName()), storageStrategy);

        args = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                ("{" +
                        "'connectionOptionsDependency':'the connection options dependency'," +
                        "'connectionPoolDependency':'the connection pool dependency'," +
                        "'collectionName':'the_collection_name'" +
                        "}").replace('\'','"'));

        addEntryQueryMessage = mock(AddEntryQueryMessage.class);
        when(addEntryQueryMessage.getEntryArguments()).thenReturn(mock(IObject.class));

        deleteEntryQueryMessage = mock(DeleteEntryQueryMessage.class);
        when(deleteEntryQueryMessage.getEntryId()).thenReturn("entry-to-delete-id");

        listEntriesQueryMessage = mock(ListEntriesQueryMessage.class);

        newEntryStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(Keys.getOrAdd("new scheduler entry"), newEntryStrategy);

        entryMock = mock(ISchedulerEntry.class);
        when(entryMock.getState()).thenReturn(mock(IObject.class));
    }

    @Test
    public void Should_downloadEntriesOnStartup()
            throws Exception {
        when(storage.downloadNextPage(anyInt()))
                .thenReturn(false)
                .thenReturn(false)
                .thenReturn(false)
                .thenReturn(true);

        assertNotNull(new SchedulerActor(args));

        verify(storage, times(4)).downloadNextPage(anyInt());
    }

    @Test
    public void Should_createNewEntry()
            throws Exception {
        SchedulerActor actor = new SchedulerActor(args);

        actor.addEntry(addEntryQueryMessage);

        verify(newEntryStrategy).resolve(same(addEntryQueryMessage.getEntryArguments()), same(storage));
    }

    @Test
    public void Should_deleteEntry()
            throws Exception {
        when(storage.getEntry("entry-to-delete-id")).thenReturn(entryMock);

        SchedulerActor actor = new SchedulerActor(args);

        actor.deleteEntry(deleteEntryQueryMessage);

        verify(entryMock).cancel();
    }

    @Test
    public void Should_listEntries()
            throws Exception {
        when(storage.listLocalEntries()).thenReturn(Collections.singletonList(entryMock));
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        SchedulerActor actor = new SchedulerActor(args);

        actor.listEntries(listEntriesQueryMessage);

        verify(listEntriesQueryMessage, times(1)).setEntries(captor.capture());

        assertEquals(1, captor.getValue().size());
        assertSame(entryMock.getState(), captor.getValue().get(0));
    }
}