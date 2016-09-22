package info.smart_tools.smartactors.test.test_environment_handler;

import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.initialization_exception.InitializationException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.message_processing.exceptions.MessageReceiveException;
import info.smart_tools.smartactors.core.message_processing.exceptions.NestedChainStackOverflowException;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link MainTestChain}.
 */
public class MainTestChainTest {

    private IStrategyContainer container = new StrategyContainer();
    private IAction<Throwable> completionCallbackMock;
    private IObject successArgumentsMock;
    private IReceiverChain testingChain;

    @Before
    public void setUp()
            throws Exception {
        this.completionCallbackMock = mock(IAction.class);
        this.successArgumentsMock = mock(IObject.class);
        this.testingChain = mock(IReceiverChain.class);

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
                IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.getCanonicalName()),
                new ApplyFunctionToArgumentsStrategy((args) -> new DSObject())
        );
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                new ApplyFunctionToArgumentsStrategy((args) -> new FieldName((String) args[0]))
        );
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrowWhenCallbackIsNull()
            throws Exception {
        assertNotNull(new MainTestChain(this.testingChain, null, this.successArgumentsMock));
        fail();
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrowWhenChainIsNull()
            throws Exception {
        assertNotNull(new MainTestChain(null, this.completionCallbackMock, this.successArgumentsMock));
        fail();
    }

    @Test(expected = InitializationException.class)
    public void Should_constructorThrowInitializationExceptionWhenIOCNotInitialized()
            throws Exception {
        IResolveDependencyStrategy strategy = mock(IResolveDependencyStrategy.class);
        IOC.register(IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.getCanonicalName()), strategy);
        doThrow(Exception.class).when(strategy).resolve();
        assertNotNull(new MainTestChain(this.testingChain, this.completionCallbackMock, this.successArgumentsMock));
        fail();
    }

    @Test
    public void Should_initSuccessReceiverArgs()
            throws Exception {
        IReceiverChain chain = new MainTestChain(this.testingChain, this.completionCallbackMock, null);
        assertNotNull(chain.get(1));
    }

    @Test
    public void Should_haveName()
            throws Exception {
        IReceiverChain chain = new MainTestChain(this.testingChain, this.completionCallbackMock, this.successArgumentsMock);
        assertNotNull(chain.getName());
    }

    @Test
    public void Should_returnSuccessReceiverArgumentsForFirstReceiverInChain()
            throws Exception {
        IReceiverChain chain = new MainTestChain(this.testingChain, this.completionCallbackMock, this.successArgumentsMock);
        assertSame(this.successArgumentsMock, chain.getArguments(1));
        assertNotNull(chain.getArguments(0));
    }

    @Test
    public void Should_returnSuccessReceiverForFirstAndSecondReceiverInChain()
            throws Exception {
        IReceiverChain chain = new MainTestChain(this.testingChain, this.completionCallbackMock, this.successArgumentsMock);
        assertNotNull(chain.get(0));
        assertNotNull(chain.get(1));
        assertNull(chain.get(2));
    }

    @Test
    public void Should_callCallbackWhenChainCompletedWithException()
            throws Exception {
        Throwable exceptionMock = mock(Throwable.class);

        IReceiverChain chain = new MainTestChain(this.testingChain, this.completionCallbackMock, this.successArgumentsMock);

        chain.getExceptionalChainAndEnvironments(exceptionMock);
        chain.getExceptionalChainAndEnvironments(exceptionMock);

        verify(this.completionCallbackMock, times(1)).execute(same(exceptionMock));
    }

    @Test
    public void Should_callCallbackWhenChainCompletedSuccessful()
            throws Exception {
        IReceiverChain chain = new MainTestChain(this.testingChain, this.completionCallbackMock, this.successArgumentsMock);

        IMessageProcessor mpMock = mock(IMessageProcessor.class);
        IMessageProcessingSequence mpsMock = mock(IMessageProcessingSequence.class);
        when(mpMock.getSequence()).thenReturn(mpsMock);

        chain.get(0).receive(mpMock);
        chain.get(1).receive(mpMock);

        verify(this.completionCallbackMock, times(1)).execute(null);
    }

    @Test(expected = MessageReceiveException.class)
    public void Should_successReceiverWrapExceptionThrownByCallback()
            throws Exception {
        IReceiverChain chain = new MainTestChain(this.testingChain, this.completionCallbackMock, this.successArgumentsMock);
        IMessageProcessor mpMock = mock(IMessageProcessor.class);

        doThrow(ActionExecuteException.class).when(this.completionCallbackMock).execute(null);

        chain.get(1).receive(mpMock);
    }

    @Test
    public void Should_returnExceptionalTestChainWhenExceptionInCallback()
            throws Exception {
        IReceiverChain chain = new MainTestChain(this.testingChain, this.completionCallbackMock, this.successArgumentsMock);
        doThrow(InvalidArgumentException.class).when(this.completionCallbackMock).execute(any(Exception.class));
        IObject exceptionalChainAndEnv = chain.getExceptionalChainAndEnvironments(new Exception());
        IReceiverChain exceptionalChain = (IReceiverChain) exceptionalChainAndEnv.getValue(new FieldName("chain"));
        assertNotNull(exceptionalChain);
    }

    @Test (expected = MessageReceiveException.class)
    public void Should_throwMessageReceiverExceptionOnFirstReceiverFail()
            throws Exception {
        IReceiverChain chain = new MainTestChain(this.testingChain, this.completionCallbackMock, this.successArgumentsMock);

        IMessageProcessor mpMock = mock(IMessageProcessor.class);
        doThrow(NestedChainStackOverflowException.class).when(mpMock).getSequence();
        chain.get(0).receive(mpMock);
        fail();
    }
}
