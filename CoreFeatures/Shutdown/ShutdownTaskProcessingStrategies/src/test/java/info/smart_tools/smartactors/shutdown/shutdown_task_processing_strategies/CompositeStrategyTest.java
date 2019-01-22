package info.smart_tools.smartactors.shutdown.shutdown_task_processing_strategies;

import info.smart_tools.smartactors.base.interfaces.istrategy.IStrategy;
import info.smart_tools.smartactors.base.interfaces.istrategy.exception.StrategyException;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.itask_execution_state.ITaskExecutionState;
import info.smart_tools.smartactors.task.itask_preprocess_strategy.ITaskProcessStrategy;
import org.junit.Test;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Test for {@link CompositeStrategy}.
 */
public class CompositeStrategyTest extends PluginsLoadingTestBase {
    private IStrategy strategyStrategy;
    private ITaskProcessStrategy defaultStrategyMock, customStrategyMock;
    private IKey strategyStrategyKey;
    private ITaskExecutionState taskExecutionState;

    private abstract class ATaskSubclass implements ITask {}

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
        strategyStrategy = mock(IStrategy.class);
        defaultStrategyMock = mock(ITaskProcessStrategy.class);
        customStrategyMock = mock(ITaskProcessStrategy.class);
        strategyStrategyKey = Keys.resolveByName("key");
        taskExecutionState = mock(ITaskExecutionState.class);

        IOC.register(strategyStrategyKey, strategyStrategy);
    }

    @Test
    public void Should_useDefaultStrategyWhenThereIsNoSpecialStrategy()
            throws Exception {
        when(taskExecutionState.getTaskClass()).thenReturn((Class) ATaskSubclass.class);
        when(strategyStrategy.resolve(same(ATaskSubclass.class)))
                .thenThrow(StrategyException.class);
        ITaskProcessStrategy strategy = new CompositeStrategy(strategyStrategyKey, defaultStrategyMock);

        strategy.process(taskExecutionState);

        verify(defaultStrategyMock).process(same(taskExecutionState));
    }

    @Test
    public void Should_useSpecialStrategyWhenAvailable()
            throws Exception {
        when(taskExecutionState.getTaskClass()).thenReturn((Class) ATaskSubclass.class);
        when(strategyStrategy.resolve(same(ATaskSubclass.class)))
                .thenReturn(customStrategyMock);
        ITaskProcessStrategy strategy = new CompositeStrategy(strategyStrategyKey, defaultStrategyMock);

        strategy.process(taskExecutionState);

        verify(customStrategyMock).process(same(taskExecutionState));
    }
}
