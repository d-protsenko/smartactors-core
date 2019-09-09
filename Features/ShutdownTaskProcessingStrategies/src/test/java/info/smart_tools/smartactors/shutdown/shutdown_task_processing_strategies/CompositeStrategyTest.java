package info.smart_tools.smartactors.shutdown.shutdown_task_processing_strategies;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.istrategy.IStrategy;
import info.smart_tools.smartactors.base.interfaces.istrategy.exception.StrategyException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc.recursive_strategy_container.StrategyContainer;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.string_ioc_key.Key;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.iscope.exception.ScopeException;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.itask_execution_state.ITaskExecutionState;
import info.smart_tools.smartactors.task.itask_preprocess_strategy.ITaskProcessStrategy;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Test for {@link CompositeStrategy}.
 */
public class CompositeStrategyTest {
    private IStrategy strategyStrategy;
    private ITaskProcessStrategy defaultStrategyMock, customStrategyMock;
    private IKey strategyStrategyKey;
    private ITaskExecutionState taskExecutionState;

    private abstract class ATaskSubclass implements ITask {}

    @Before
    public void initialize() throws Exception {
        ScopeProvider.clearListOfSubscribers();
        ScopeProvider.subscribeOnCreationNewScope(
                scope -> {
                    try {
                        IStrategyContainer parentContainer = null;
                        try {
                            parentContainer = (IStrategyContainer) scope.getValue(IOC.getIocKey());
                        } catch (ScopeException e) {
                            //
                        }
                        scope.setValue(IOC.getIocKey(), new StrategyContainer(parentContainer));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        Object parentScopeKey = ScopeProvider.createScope(null);
        IScope scope = ScopeProvider.getScope(parentScopeKey);

        ScopeProvider.setCurrentScope(scope);
        IOC.register(IOC.getKeyForKeyByNameStrategy(), new ResolveByNameIocStrategy(
                (a) -> {
                    try {
                        return new Key((String) a[0]);
                    } catch (InvalidArgumentException e) {
                        throw new RuntimeException(e);
                    }
                })
        );

        registerMocks();
    }

    protected void registerMocks() throws Exception {
        strategyStrategy = mock(IStrategy.class);
        defaultStrategyMock = mock(ITaskProcessStrategy.class);
        customStrategyMock = mock(ITaskProcessStrategy.class);
        strategyStrategyKey = Keys.getKeyByName("key");
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
