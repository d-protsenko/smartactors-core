package info.smart_tools.smartactors.debugger.session_impl;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.debugger.interfaces.IDebuggerSequence;
import info.smart_tools.smartactors.debugger.interfaces.IDebuggerSession;
import info.smart_tools.smartactors.debugger.interfaces.exceptions.CommandExecutionException;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Test for {@link DebuggerSessionImpl}.
 */
public class DebuggerSessionImplTest extends PluginsLoadingTestBase {
    private IChainStorage chainStorageMock;
    private Object debuggerAddress = new Object();
    private IReceiverChain chainMock = mock(IReceiverChain.class);

    private IMessageProcessingSequence innerSequenceMock = mock(IMessageProcessingSequence.class);
    private IDebuggerSequence debuggerSequenceMock;
    private IMessageProcessor messageProcessorMock;
    private IResolveDependencyStrategy sequenceStrategyMock;
    private IResolveDependencyStrategy debuggerSequenceStrategyMock;
    private IResolveDependencyStrategy processorStrategyMock;
    private Object taskQueue = new Object();

    private IDebuggerSession session;

    private Exception exception1 = new Exception();

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
        IOC.register(Keys.getOrAdd("chain_id_from_map_name"), new IResolveDependencyStrategy() {
            @Override
            public <T> T resolve(Object... args) throws ResolveDependencyStrategyException {
                return (T) args[0].toString().concat("__id");
            }
        });

        chainStorageMock = mock(IChainStorage.class);
        IOC.register(Keys.getOrAdd(IChainStorage.class.getCanonicalName()), new SingletonStrategy(chainStorageMock));
        when(chainStorageMock.resolve(eq("the-chain__id"))).thenReturn(chainMock);

        debuggerSequenceMock = mock(IDebuggerSequence.class);
        messageProcessorMock = mock(IMessageProcessor.class);

        sequenceStrategyMock = mock(IResolveDependencyStrategy.class);
        debuggerSequenceStrategyMock = mock(IResolveDependencyStrategy.class);
        processorStrategyMock = mock(IResolveDependencyStrategy.class);
        when(sequenceStrategyMock.resolve(eq(12), same(chainMock)))
                .thenReturn(innerSequenceMock)
                .thenThrow(ResolveDependencyStrategyException.class);
        when(debuggerSequenceStrategyMock.resolve(same(innerSequenceMock), same(debuggerAddress)))
                .thenReturn(debuggerSequenceMock)
                .thenThrow(ResolveDependencyStrategyException.class);
        when(processorStrategyMock.resolve(same(taskQueue), same(debuggerSequenceMock)))
                .thenReturn(messageProcessorMock)
                .thenThrow(ResolveDependencyStrategyException.class);
        IOC.register(Keys.getOrAdd(IMessageProcessingSequence.class.getCanonicalName()), sequenceStrategyMock);
        IOC.register(Keys.getOrAdd("new debugger sequence"), debuggerSequenceStrategyMock);
        IOC.register(Keys.getOrAdd(IMessageProcessor.class.getCanonicalName()), processorStrategyMock);
        IOC.register(Keys.getOrAdd("task_queue"), new SingletonStrategy(taskQueue));

        IOC.register(Keys.getOrAdd("value_dependency"), new IResolveDependencyStrategy() {
            @Override
            public <T> T resolve(Object... args) throws ResolveDependencyStrategyException {
                return (T) args[0].toString().concat("_value");
            }
        });
    }

    private Object c(final String name, final Object arg) throws CommandExecutionException, InvalidArgumentException {
        return session.executeCommand(name, arg);
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_throwWhenUnknownCommandRequired()
            throws Exception {
        session = new DebuggerSessionImpl("the-id", debuggerAddress);

        c("unknown", null);
    }

    @Test
    public void Should_changeAndReturnStepModeLevel()
            throws Exception {
        session = new DebuggerSessionImpl("the-id", debuggerAddress);

        assertEquals("OK", c("stepMode", 3));
        assertEquals(3, c("getStepMode", null));

        assertEquals("OK", c("stepMode", "4"));
        assertEquals(4, c("getStepMode", null));

        assertEquals("OK", c("stepMode", "all"));
        assertEquals(Integer.MAX_VALUE, c("getStepMode", null));

        assertEquals("OK", c("stepMode", "none"));
        assertEquals(-1, c("getStepMode", null));
    }

    @Test
    public void Should_startDebugging()
            throws Exception {
        session = new DebuggerSessionImpl("the-id", debuggerAddress);

        try {
            c("start", null);
            fail("should not start when there is no message set and no chain selected");
        } catch (CommandExecutionException ignore) {}

        c("setMessage", IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{'a':'foo','b':'bar'}".replace('\'', '"')));
        c("setChain", "the-chain");
        c("setStackDepth", 12.2);

        assertEquals(false, c("isPaused", null));
        assertEquals(false, c("isRunning", null));

        assertEquals("OK", c("start", null));
        verify(messageProcessorMock).process(isNotNull(IObject.class), isNotNull(IObject.class));
        reset(messageProcessorMock);

        assertEquals(false, c("isPaused", null));
        assertEquals(true, c("isRunning", null));
    }

    @Test(expected = CommandExecutionException.class)
    public void Should_notChangeMainChainWhenIsStarted()
            throws Exception {
        Should_startDebugging();
        c("setChain", "the-chain");
    }

    @Test
    public void Should_pauseMessageProcessingWhenStepModeIsEnabled()
            throws Exception {
        Should_startDebugging();

        c("stepMode", "all");

        session.handleInterrupt(messageProcessorMock);
        verify(messageProcessorMock).pauseProcess();

        assertEquals(true, c("isPaused", null));
        assertEquals(false, c("isRunning", null));
    }

    @Test
    public void Should_notPauseMessageProcessingWhenStepModeIsDisabled()
            throws Exception {
        Should_startDebugging();

        c("stepMode", "none");

        session.handleInterrupt(messageProcessorMock);
        verify(messageProcessorMock, never()).pauseProcess();

        assertEquals(false, c("isPaused", null));
        assertEquals(true, c("isRunning", null));
    }

    @Test
    public void Should_pauseProcessingWhenExceptionOccursAndBreakOnExceptionIsTrue()
            throws Exception {
        Should_startDebugging();

        c("stepMode", "none");
        c("setBreakOnException", true);

        when(debuggerSequenceMock.getException()).thenReturn(exception1);
        session.handleInterrupt(messageProcessorMock);
        verify(messageProcessorMock).pauseProcess();
        verify(debuggerSequenceMock, never()).processException();
    }

    @Test
    public void Should_tryToProcessExceptionWhenExceptionOccursAndBreakOnExceptionIsFalse()
            throws Exception {
        Should_startDebugging();

        c("stepMode", "none");
        c("setBreakOnException", false);

        when(debuggerSequenceMock.getException()).thenReturn(exception1);
        when(debuggerSequenceMock.processException()).thenReturn(true);
        session.handleInterrupt(messageProcessorMock);
        verify(debuggerSequenceMock).processException();
    }

    @Test
    public void Should_tryToProcessExceptionWhenExceptionOccursAndBreakOnExceptionIsFalse1()
            throws Exception {
        Should_startDebugging();

        c("stepMode", "none");
        c("setBreakOnException", false);

        when(debuggerSequenceMock.getException()).thenReturn(exception1);
        when(debuggerSequenceMock.processException()).thenReturn(false);
        session.handleInterrupt(messageProcessorMock);
        verify(debuggerSequenceMock).processException();
        verify(messageProcessorMock).pauseProcess();
    }

    @Test(expected = CommandExecutionException.class)
    public void Should_throwWhenProcessExceptionCalledWhenThereIsNoExceptionOccurred()
            throws Exception {
        Should_startDebugging();

        c("processException", null);
    }

    @Test
    public void Should_setSeparateMessageFields()
            throws Exception {
        Should_startDebugging();

        c("stop", null);

        c("setMessageField", IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{'name':'z','value':'x','dependency':'value_dependency'}".replace('\'', '"')));
        c("setMessageField", IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{'name':'zz','value':'e'}".replace('\'', '"')));

        IObject msg = (IObject) c("getMessage", null);

        assertEquals("x_value", msg.getValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "z")));
        assertEquals("e", msg.getValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "zz")));
        assertEquals("foo", msg.getValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "a")));
        assertEquals("bar", msg.getValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "b")));
    }

    @Test
    public void Should_createStackTrace()
            throws Exception {
        Should_startDebugging();

        session.handleInterrupt(messageProcessorMock);

        when(debuggerSequenceMock.getCurrentLevel()).thenReturn(2);
        when(debuggerSequenceMock.getStepAtLevel(0)).thenReturn(1);
        when(debuggerSequenceMock.getStepAtLevel(1)).thenReturn(3);
        when(debuggerSequenceMock.getStepAtLevel(2)).thenReturn(2);
        List<IObject> trace = (List) c("getStackTrace", null);

        assertEquals(3, trace.size());
    }

    @Test
    public void Should_pauseMessageProcessingAfterPauseCommand()
            throws Exception {
        Should_startDebugging();

        assertEquals(false, c("isPaused", null));
        assertEquals(true, c("isRunning", null));

        assertEquals(true, c("pause", null));

        assertEquals(false, c("isPaused", null));
        assertEquals(true, c("isRunning", null));

        session.handleInterrupt(messageProcessorMock);
        verify(messageProcessorMock).pauseProcess();
        reset(messageProcessorMock);

        assertEquals(true, c("isPaused", null));
        assertEquals(false, c("isRunning", null));

        assertEquals("OK", c("continue", null));

        assertEquals(false, c("isPaused", null));
        assertEquals(true, c("isRunning", null));
    }

    @Test
    public void Should_stopSequenceWhenClosed()
            throws Exception {
        Should_startDebugging();

        session.close();

        verify(debuggerSequenceMock).stop();
    }
}
