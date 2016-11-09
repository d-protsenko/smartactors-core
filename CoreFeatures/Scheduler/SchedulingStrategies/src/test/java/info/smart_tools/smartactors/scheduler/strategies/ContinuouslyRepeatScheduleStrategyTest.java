package info.smart_tools.smartactors.scheduler.strategies;

import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulingStrategy;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.EntryScheduleException;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.SchedulingStrategyExecutionException;
import info.smart_tools.smartactors.field_plugins.ifield_plugin.IFieldPlugin;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test for {@link ContinuouslyRepeatScheduleStrategy}.
 */
public class ContinuouslyRepeatScheduleStrategyTest extends PluginsLoadingTestBase {
    private ISchedulerEntry entry;
    private IFieldName start;
    private IFieldName interval;

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
        entry = mock(ISchedulerEntry.class);
        when(entry.getState()).thenReturn(IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName())));

        start = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "start");
        interval = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "interval");
    }

    @Test
    public void Should_cancelEntryWhenExceptionOccurs()
            throws Exception {
        ISchedulingStrategy strategy = new ContinuouslyRepeatScheduleStrategy();

        strategy.processException(entry, mock(Throwable.class));

        verify(entry).cancel();
    }

    @Test(expected = SchedulingStrategyExecutionException.class)
    public void Should_wrapExceptionWhenEntryCancellationThrows()
            throws Exception {
        doThrow(EntryScheduleException.class).when(entry).cancel();

        ISchedulingStrategy strategy = new ContinuouslyRepeatScheduleStrategy();

        strategy.processException(entry, mock(Throwable.class));
    }

    @Test
    public void Should_initializeEntry()
            throws Exception {
        ISchedulingStrategy strategy = new ContinuouslyRepeatScheduleStrategy();

        strategy.init(entry,
                IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                    "{'start':'3410-01-01T00:00:01','interval':'PT24H'}".replace('\'','"')));

        // Happy new year to the people of 3410'th year (if you are alive) (if you still use smartactors)
        assertEquals("3410-01-01T00:00:01", entry.getState().getValue(start));
        assertEquals("PT24H", entry.getState().getValue(interval));

        verify(entry).scheduleNext(LocalDateTime.parse("3410-01-01T00:00:01").atZone(ZoneOffset.UTC).toInstant().toEpochMilli());
    }

    @Test
    public void Should_initializeEntryUsingCurrentTimeIfNoTimeGiven()
            throws Exception {
        ISchedulingStrategy strategy = new ContinuouslyRepeatScheduleStrategy();

        strategy.init(entry,
                IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                    "{'interval':'PT24H'}".replace('\'','"')));

        assertTrue(Duration.between(
                LocalDateTime.parse((String) entry.getState().getValue(start)),
                LocalDateTime.now()).abs().getSeconds()
                    <= 1);
        assertEquals("PT24H", entry.getState().getValue(interval));
    }

    @Test
    public void Should_restoreAndPostProcessEntry()
            throws Exception {
        ISchedulingStrategy strategy = new ContinuouslyRepeatScheduleStrategy();
        when(entry.getState()).thenReturn(
                IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                        "{'start':'1994-10-15T17:32:05','interval':'PT72H'}".replace('\'','"')));

        strategy.restore(entry);

        assertEquals("1994-10-15T17:32:05", entry.getState().getValue(start));
        assertEquals("PT72H", entry.getState().getValue(interval));
        ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(long.class);
        verify(entry).scheduleNext(timeCaptor.capture());
        long captured0 = timeCaptor.getValue();
        assertEquals(0,
                (captured0 - LocalDateTime.parse("1994-10-15T17:32:05").atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
                % Duration.parse("PT72H").toMillis());
        assertTrue(System.currentTimeMillis() < captured0);

        when(entry.getLastTime()).thenReturn(captured0);

        strategy.postProcess(entry);

        verify(entry, times(2)).scheduleNext(timeCaptor.capture());
        assertEquals(Duration.parse("PT72H").toMillis(), timeCaptor.getValue() - captured0);
    }
}
