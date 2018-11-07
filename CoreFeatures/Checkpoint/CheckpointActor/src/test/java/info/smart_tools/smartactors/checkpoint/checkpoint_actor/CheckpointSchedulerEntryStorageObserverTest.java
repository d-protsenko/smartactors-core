package info.smart_tools.smartactors.checkpoint.checkpoint_actor;

import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

/**
 * Test for {@link CheckpointSchedulerEntryStorageObserver}.
 */
public class CheckpointSchedulerEntryStorageObserverTest extends PluginsLoadingTestBase {

    @Override
    protected void loadPlugins() throws Exception {
        load(ScopedIOCPlugin.class);
        load(PluginScopeProvider.class);
        load(PluginIOCKeys.class);
        load(PluginDSObject.class);
        load(IFieldNamePlugin.class);
    }

    private ISchedulerEntry em(final String id) throws Exception {
        IObject state = IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.iobject.IObject"),
                String.format("{'prevCheckpointEntryId':'%s'}", id).replace('\'','"'));

        ISchedulerEntry entry = mock(ISchedulerEntry.class);

        when(entry.getState()).thenReturn(state);

        return entry;
    }

    @Test
    public void Should_updateTableWhenEntryUpdated()
            throws Exception {
        CheckpointSchedulerEntryStorageObserver observer = new CheckpointSchedulerEntryStorageObserver();

        ISchedulerEntry e1 = em("1");

        observer.onUpdateEntry(e1);

        assertSame(e1, observer.getPresentEntry("1"));
    }

    @Test
    public void Should_cancelDuplicateEntries()
            throws Exception {
        CheckpointSchedulerEntryStorageObserver observer = new CheckpointSchedulerEntryStorageObserver();

        ISchedulerEntry e11 = em("1");
        ISchedulerEntry e12 = em("1");

        observer.onUpdateEntry(e11);
        observer.onUpdateEntry(e12);

        assertSame(e12, observer.getPresentEntry("1"));
        verify(e11).cancel();
    }

    @Test
    public void Should_dropEntryFromTableWhenItIsCancelled()
            throws Exception {
        CheckpointSchedulerEntryStorageObserver observer = new CheckpointSchedulerEntryStorageObserver();

        ISchedulerEntry e1 = em("1");

        observer.onUpdateEntry(e1);
        observer.onCancelEntry(e1);

        assertNull(observer.getPresentEntry("1"));
    }
}
