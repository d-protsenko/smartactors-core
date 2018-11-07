package info.smart_tools.smartactors.statistics.sensors.scheduled_query_sensor.query_executors;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.base.interfaces.ipool.exception.PoolTakeException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import info.smart_tools.smartactors.statistics.sensors.scheduled_query_sensor.interfaces.IQueryExecutor;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Test for {@link DatabaseCountQueryExecutor}.
 */
public class DatabaseCountQueryExecutorTest extends PluginsLoadingTestBase {
    private IResolveDependencyStrategy connectionOptionsStrategyMock;
    private IResolveDependencyStrategy connectionPoolStrategyMock;
    private IResolveDependencyStrategy taskStrategyMock;
    private IPool poolMock;
    private final Object connectionMock = new Object(), connectionOptionsMock = new Object();
    private ISchedulerEntry entryMock;
    private ITask taskMock;

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
        connectionOptionsStrategyMock = mock(IResolveDependencyStrategy.class);
        when(connectionOptionsStrategyMock.resolve()).thenReturn(connectionOptionsMock);
        IOC.register(Keys.getOrAdd("the connection options"), connectionOptionsStrategyMock);

        poolMock = mock(IPool.class);
        connectionPoolStrategyMock = mock(IResolveDependencyStrategy.class);
        when(connectionPoolStrategyMock.resolve(same(connectionOptionsMock))).thenReturn(poolMock);
        IOC.register(Keys.getOrAdd("the connection pool"), connectionPoolStrategyMock);
        when(poolMock.take()).thenReturn(connectionMock).thenThrow(PoolTakeException.class);

        taskMock = mock(ITask.class);

        taskStrategyMock = mock(IResolveDependencyStrategy.class);
        IOC.register(Keys.getOrAdd("db.collection.count"), taskStrategyMock);

        entryMock = mock(ISchedulerEntry.class);
        when(entryMock.getState()).thenReturn(IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject")));
    }

    @Test
    public void Should_queryCountOfRecordsFromDatabase()
            throws Exception {
        IQueryExecutor executor = new DatabaseCountQueryExecutor();
        IObject args = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"),
                ("{" +
                        "'collection':'the_collection'," +
                        "'connectionOptionsDependency':'the connection options'," +
                        "'connectionPoolDependency':'the connection pool'," +
                        "'filter':{'x':{'$eq':1}}" +
                        "}").replace('\'','"'));
        ArgumentCaptor<Object> argsCaptor = ArgumentCaptor.forClass(Object.class);
        when(taskStrategyMock.resolve(any(),any(),any(),any())).thenReturn(taskMock);
        doAnswer(invocation -> {
            verify(taskStrategyMock).resolve(argsCaptor.capture());
            assertSame(connectionMock, argsCaptor.getAllValues().get(0));
            assertEquals("the_collection", argsCaptor.getAllValues().get(1));
            assertSame(args.getValue(IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "filter")),
                    argsCaptor.getAllValues().get(2));
            ((IAction<Long>) argsCaptor.getAllValues().get(3)).execute(3L);
            return null;
        }).when(taskMock).execute();

        executor.init(entryMock, args);

        Collection<? extends Number> data = executor.execute(entryMock);

        assertEquals(1, data.size());
        for (Number x : data) {
            assertEquals(3L, x);
        }
    }
}
