package info.smart_tools.smartactors.checkpoint.scheduling_strategies;

import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulingStrategy;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test for {@link CheckpointFibonacciRepeatStrategy}.
 */
public class CheckpointFibonacciRepeatStrategyTest extends PluginsLoadingTestBase {
    private ISchedulerEntry entryMock;
    private IObject entryState;

    @Override
    protected void loadPlugins() throws Exception {
        load(ScopedIOCPlugin.class);
        load(PluginScopeProvider.class);
        load(PluginIOCKeys.class);
        load(PluginDSObject.class);
        load(IFieldNamePlugin.class);
    }

    @Override
    protected void registerMocks() throws Exception {

    }

    private void makeEntry(final String stateSrc) throws Exception {
        entryMock = mock(ISchedulerEntry.class);
        entryState = IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.iobject.IObject"), stateSrc.replace('\'','"'));
        when(entryMock.getState()).thenReturn(entryState);
    }

    private IObject makeArgs(final String src) throws Exception {
        return IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.iobject.IObject"), src.replace('\'','"'));
    }



    @Test
    public void Should_rescheduleEntryWithIntervalsProportionalToFibonacciNumbers()
            throws Exception {
        ISchedulingStrategy strategy = new CheckpointFibonacciRepeatStrategy();

        makeEntry("{" +
                "'remainingTimes':4," +
                "'baseInterval':'PT2M4S'," +
                "'postCompletionDelay':'PT400H'," +
                "'number1':0," +
                "'number2':1" +
                "}");

        when(entryMock.getLastTime()).thenReturn(13666L);

        // 1
        strategy.postProcess(entryMock);

        verify(entryMock).scheduleNext(eq(13666L + Duration.parse("PT2M4S").toMillis() * (1)));
        verify(entryMock).save();

        assertNull(entryState.getValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "completed")));

        reset(entryMock);

        // 2
        when(entryMock.getState()).thenReturn(entryState);
        when(entryMock.getLastTime()).thenReturn(13666L);

        strategy.postProcess(entryMock);

        verify(entryMock).scheduleNext(eq(13666L + Duration.parse("PT2M4S").toMillis() * (1 + 1)));
        verify(entryMock).save();

        assertNull(entryState.getValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "completed")));

        reset(entryMock);

        // 3
        when(entryMock.getState()).thenReturn(entryState);
        when(entryMock.getLastTime()).thenReturn(13666L);

        strategy.postProcess(entryMock);

        verify(entryMock).scheduleNext(eq(13666L + Duration.parse("PT2M4S").toMillis() * (1 + 2)));
        verify(entryMock).save();

        assertNull(entryState.getValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "completed")));

        reset(entryMock);

        // 4
        when(entryMock.getState()).thenReturn(entryState);
        when(entryMock.getLastTime()).thenReturn(13666L);

        strategy.postProcess(entryMock);

        verify(entryMock).scheduleNext(eq(13666L + Duration.parse("PT2M4S").toMillis() * (2 + 3)));
        verify(entryMock).save();

        assertNull(entryState.getValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "completed")));

        reset(entryMock);

        // 5 - end
        when(entryMock.getState()).thenReturn(entryState);
        when(entryMock.getLastTime()).thenReturn(13666L);

        strategy.postProcess(entryMock);

        verify(entryMock).scheduleNext(eq(13666L + Duration.parse("PT400H").toMillis()));
        verify(entryMock).save();

        assertNotNull(entryState.getValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "completed")));

        reset(entryMock);
    }


    @Test
    public void Should_initializeEntryState()
            throws Exception {
        ISchedulingStrategy strategy = new CheckpointFibonacciRepeatStrategy();

        makeEntry("{}");

        strategy.init(entryMock, makeArgs("{" +
                "'times':2," +
                "'baseInterval':'PT3H'" +
                "}"));

        verify(entryMock).save();
        verify(entryMock).scheduleNext(anyLong());

        assertEquals("PT3H", entryState.getValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "baseInterval")));
        assertEquals("PT3H", entryState.getValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "postRestoreDelay")));
        assertEquals("PT3H", entryState.getValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "postCompletionDelay")));
        assertEquals(2, entryState.getValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "times")));
        assertEquals(1, entryState.getValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "remainingTimes")));

        // Strategy calculated interval one time so (0,1) replaced by (1,1)
        assertEquals(1, entryState.getValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "number1")));
        assertEquals(1, entryState.getValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "number2")));
    }
}
