package info.smart_tools.smartactors.core.chain_testing;

import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.initialization_exception.InitializationException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.core.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.test.isource.ISource;
import info.smart_tools.smartactors.test.isource.exception.SourceExtractionException;
import info.smart_tools.smartactors.test.itest_runner.ITestRunner;
import info.smart_tools.smartactors.test.itest_runner.exception.TestExecutionException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link HttpEndpointTestRunner}.
 */
public class HttpEndpointTestRunnerTest {

    private IStrategyContainer container = new StrategyContainer();
    private Object chainId = mock(Object.class);
    private IChainStorage chainStorage = mock(IChainStorage.class);
    private IReceiverChain receiverChain = mock(IReceiverChain.class);
    private IObject sourceObject = mock(IObject.class);
    private ISource source = mock(ISource.class);

    @Before
    public void init()
            throws Exception {
        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope scope = ScopeProvider.getScope(keyOfMainScope);
        scope.setValue(IOC.getIocKey(), this.container);
        ScopeProvider.setCurrentScope(scope);

        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            try {
                                return new Key((String) a[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                new ApplyFunctionToArgumentsStrategy(
                        (a) -> {
                            try {
                                return new FieldName((String) a[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
        );
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.getCanonicalName()),
                new ApplyFunctionToArgumentsStrategy(
                        (a) -> {
                            try {
                                return this.sourceObject;
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
        );
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), "chain_id_from_map_name"),
                new ApplyFunctionToArgumentsStrategy((a) -> {
                    return this.chainId;
                })
        );
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IChainStorage.class.getCanonicalName()),
                new SingletonStrategy(this.chainStorage)
        );
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), "test_data_source"),
                new SingletonStrategy(this.source)
        );
        when(this.chainStorage.resolve(this.chainId)).thenReturn(this.receiverChain);
    }

    @Test
    public void checkCreation()
            throws Exception {
        ITestRunner runner = new HttpEndpointTestRunner();
        assertNotNull(runner);
    }

    @Test
    public void checkRunnerHandle()
            throws Exception {
        ITestRunner runner = new HttpEndpointTestRunner();
        IObject desc = mock(IObject.class);
        IAction<Throwable> callback = new IAction<Throwable>() {
            @Override
            public void execute(Throwable actingObject)
                    throws ActionExecuteException, InvalidArgumentException {
            }
        };
        when(desc.getValue(new FieldName("chainName"))).thenReturn("chain");
        runner.runTest(desc, callback);
        verify(this.source, times(1)).setSource(this.sourceObject);
        verify(this.sourceObject, times(1)).setValue(new FieldName("content"), desc);
        verify(this.sourceObject, times(1)).setValue(new FieldName("callback"), callback);
        verify(this.sourceObject, times(1)).setValue(new FieldName("chainName"), this.receiverChain);
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnNullFirstArgument()
            throws Exception {
        IAction<Throwable> callback = new IAction<Throwable>() {
            @Override
            public void execute(Throwable actingObject)
                    throws ActionExecuteException, InvalidArgumentException {
            }
        };
        ITestRunner runner = new HttpEndpointTestRunner();
        runner.runTest(null, callback);
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnNullSecondArgument()
            throws Exception {
        ITestRunner runner = new HttpEndpointTestRunner();
        runner.runTest(mock(IObject.class), null);
        fail();
    }

    @Test (expected = InitializationException.class)
    public void checkInitializationExceptionOnCreation()
            throws Exception {
        IResolveDependencyStrategy strategy = mock(IResolveDependencyStrategy.class);
        doThrow(ResolutionException.class).when(strategy).resolve(any());
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                strategy
        );
        new HttpEndpointTestRunner();
        fail();
    }

    @Test (expected = TestExecutionException.class)
    public void checkTestExecutionExceptionOnHandle()
            throws Exception {
        ITestRunner runner = new HttpEndpointTestRunner();
        IObject desc = mock(IObject.class);
        IAction<Throwable> callback = new IAction<Throwable>() {
            @Override
            public void execute(Throwable actingObject)
                    throws ActionExecuteException, InvalidArgumentException {
            }
        };
        when(desc.getValue(new FieldName("chainName"))).thenReturn("chain");
        doThrow(SourceExtractionException.class).when(this.source).setSource(this.sourceObject);
        runner.runTest(desc, callback);
        fail();
    }
}
