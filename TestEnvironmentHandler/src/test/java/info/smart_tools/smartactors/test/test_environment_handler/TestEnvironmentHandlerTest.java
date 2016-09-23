package info.smart_tools.smartactors.test.test_environment_handler;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.core.ienvironment_handler.exception.EnvironmentHandleException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.base.exception.initialization_exception.InitializationException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.message_processing.exceptions.NestedChainStackOverflowException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.plugin.dsobject.PluginDSObject;
import info.smart_tools.smartactors.plugin.ifieldname.IFieldNamePlugin;
import info.smart_tools.smartactors.plugin.ioc_keys.PluginIOCKeys;
import info.smart_tools.smartactors.plugin.scope_provider.PluginScopeProvider;
import info.smart_tools.smartactors.plugin.scoped_ioc.ScopedIOCPlugin;
import info.smart_tools.smartactors.test.iresult_checker.IResultChecker;
import info.smart_tools.smartactors.testing.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link TestEnvironmentHandler}.
 */
public class TestEnvironmentHandlerTest extends PluginsLoadingTestBase {
    private IObject messageMock;
    private IObject contextMock;
    private IMessageProcessor messageProcessorMock;
    private IQueue<ITask> taskQueueMock;
    private IReceiverChain testedChainMock;
    private MainTestChain mainTestChainMock;
    private IMessageProcessingSequence sequenceMock;
    private IAction<Throwable> callbackMock;

    private IResolveDependencyStrategy mainTestChainStrategyMock;
    private IResolveDependencyStrategy sequenceStrategyMock;
    private IResolveDependencyStrategy mpStrategyMock;
    private IResolveDependencyStrategy createAssertCheckerStrategyMock;
    private IResolveDependencyStrategy createInterceptCheckerStrategyMock;
    private IResultChecker assertChecker;
    private IResultChecker interceptChecker;

//    private ArgumentCaptor<IAction> cbCaptor = ArgumentCaptor.forClass(IAction.class);

    @Override
    protected void loadPlugins()
            throws Exception {
        load(ScopedIOCPlugin.class);
        load(PluginScopeProvider.class);
        load(PluginIOCKeys.class);
        load(PluginDSObject.class);
        load(IFieldNamePlugin.class);
    }

    @Override
    protected void registerMocks()
            throws Exception {
        messageMock = mock(IObject.class);
        contextMock = mock(IObject.class);
        messageProcessorMock = mock(IMessageProcessor.class);
        taskQueueMock = mock(IQueue.class);
        testedChainMock = mock(IReceiverChain.class);
        mainTestChainMock = mock(MainTestChain.class);
        sequenceMock = mock(IMessageProcessingSequence.class);
        callbackMock = mock(IAction.class);
        createAssertCheckerStrategyMock = mock(IResolveDependencyStrategy.class);
        createInterceptCheckerStrategyMock = mock(IResolveDependencyStrategy.class);
        assertChecker = mock(IResultChecker.class);
        interceptChecker = mock(IResultChecker.class);

        when(messageProcessorMock.getSequence()).thenReturn(sequenceMock);

        mainTestChainStrategyMock = mock(IResolveDependencyStrategy.class);
        sequenceStrategyMock = mock(IResolveDependencyStrategy.class);
        mpStrategyMock = mock(IResolveDependencyStrategy.class);

        when(mpStrategyMock.resolve(same(taskQueueMock), same(sequenceMock))).thenReturn(messageProcessorMock);
        when(sequenceStrategyMock.resolve(any(), same(mainTestChainMock))).thenReturn(sequenceMock);
        when(mainTestChainStrategyMock.resolve(any(IReceiverChain.class), any(IAction.class), any(IObject.class))).thenReturn(mainTestChainMock);
        when(createAssertCheckerStrategyMock.resolve(any(ArrayList.class))).thenReturn(assertChecker);
        when(createInterceptCheckerStrategyMock.resolve(any(IObject.class))).thenReturn(interceptChecker);

        IOC.register(Keys.getOrAdd("task_queue"), new SingletonStrategy(taskQueueMock));
        IOC.register(Keys.getOrAdd(MainTestChain.class.getCanonicalName()), mainTestChainStrategyMock);
        IOC.register(Keys.getOrAdd(IMessageProcessingSequence.class.getCanonicalName()), sequenceStrategyMock);
        IOC.register(Keys.getOrAdd(IMessageProcessor.class.getCanonicalName()), mpStrategyMock);
        IOC.register(Keys.getOrAdd(IResultChecker.class.getCanonicalName() + "#assert"), createAssertCheckerStrategyMock);
        IOC.register(Keys.getOrAdd(IResultChecker.class.getCanonicalName() + "#intercept"), createInterceptCheckerStrategyMock);
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_throwWhenDescriptionIsNull()
            throws Exception {
        new TestEnvironmentHandler().handle(null, mock(IReceiverChain.class), mock(IAction.class));
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_throwWhenCallbackIsNull()
            throws Exception {
        new TestEnvironmentHandler().handle(mock(IObject.class), mock(IReceiverChain.class), null);
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_throwWhenChainIsNull()
            throws Exception {
        new TestEnvironmentHandler().handle(mock(IObject.class), null, mock(IAction.class));
    }

    @Test
    public void Should_runTestWithAssert()
            throws Exception {
        IObject env = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        env.setValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "message"), messageMock);
        env.setValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "context"), contextMock);

        IObject desc = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{'name': 'The test', 'chainName': 'chainToTest', 'assert': []}".replace('\'','"'));

        desc.setValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "environment"), env);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                callbackMock.execute(null);
                return null;
            }
        }).when(this.messageProcessorMock).process(any(), any());

        new TestEnvironmentHandler().handle(desc, testedChainMock, callbackMock);

        verify(callbackMock).execute(null);
    }

    @Test
    public void Should_runTestWithIntercept()
            throws Exception {
        IObject env = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        env.setValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "message"), messageMock);
        env.setValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "context"), contextMock);

        IObject desc = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{'name': 'The test', 'chainName': 'chainToTest', 'intercept': {}}".replace('\'','"'));

        desc.setValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "environment"), env);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                callbackMock.execute(null);
                return null;
            }
        }).when(this.messageProcessorMock).process(any(), any());

        new TestEnvironmentHandler().handle(desc, testedChainMock, callbackMock);

        verify(callbackMock).execute(null);
    }


    @Test
    public void Should_runTestWhenItFails()
            throws Exception {
        IObject env = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        env.setValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "message"), messageMock);
        env.setValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "context"), contextMock);

        IObject desc = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{'name': 'The test', 'chainName': 'chainToTest', 'assert': []}".replace('\'','"'));

        desc.setValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "environment"), env);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                callbackMock.execute(new Exception());
                return null;
            }
        }).when(this.messageProcessorMock).process(any(), any());

        new TestEnvironmentHandler().handle(desc, testedChainMock, callbackMock);

        verify(callbackMock).execute(any());
    }

    @Test (expected = EnvironmentHandleException.class)
    public void Should_throwWhenDescriptionDoesNotContainsAssertAndInterceptSection()
            throws Exception {
        IObject env = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        env.setValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "message"), messageMock);
        env.setValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "context"), contextMock);

        IObject desc = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{'name': 'The test', 'chainName': 'chainToTest'}".replace('\'','"'));

        desc.setValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "environment"), env);

        new TestEnvironmentHandler().handle(desc, testedChainMock, callbackMock);
        fail();
    }

    @Test (expected = EnvironmentHandleException.class)
    public void Should_throwWhenDescriptionContainsBothAssertAndInterceptSection()
            throws Exception {
        IObject env = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        env.setValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "message"), messageMock);
        env.setValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "context"), contextMock);

        IObject desc = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{'name': 'The test', 'chainName': 'chainToTest', 'intercept': {}, 'assert': []}".replace('\'','"'));

        desc.setValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "environment"), env);

        new TestEnvironmentHandler().handle(desc, testedChainMock, callbackMock);
        fail();
    }

    @Test(expected = EnvironmentHandleException.class)
    public void Should_throwWhenDescriptionContainsFieldOfUnexpectedType()
            throws Exception {
        IObject desc = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{'name': 'The test', 'chainName': 'chainToTest', 'assert': [], 'environment': []}".replace('\'','"'));

        new TestEnvironmentHandler().handle(desc, testedChainMock, callbackMock);
        fail();
    }

    @Test (expected = InitializationException.class)
    public void Should_throwWhenIOCNotInitialized()
            throws Exception {
        IResolveDependencyStrategy strategy = mock(IResolveDependencyStrategy.class);
        IOC.register(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), strategy);
        doThrow(Exception.class).when(strategy).resolve(any());
        new TestEnvironmentHandler().handle(mock(IObject.class), testedChainMock, callbackMock);
        fail();
    }
}
