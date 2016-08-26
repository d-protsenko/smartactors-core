package info.smart_tools.smartactors.test.test_http_endpoint;

import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iasync_service.IAsyncService;
import info.smart_tools.smartactors.core.ichannel_handler.IChannelHandler;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import info.smart_tools.smartactors.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.test.isource.ISource;
import info.smart_tools.smartactors.test.test_data_source_iobject.IObjectDataSource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link TestHttpEndpoint}.
 */
public class TestHttpEndpointTest {

    private IStrategyContainer container = new StrategyContainer();
    private IReceiverChain chain = mock(IReceiverChain.class);
    private IAction<Throwable> callback = mock(IAction.class);

    @Rule
    public Timeout globalTimeout = Timeout.seconds(20);

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
    }

    @Test
    public void checkCreationAndExecution()
            throws Exception {
        IEnvironmentHandler handler = mock(IEnvironmentHandler.class);
        ISource<IObject, IObject> source = new IObjectDataSource();
        initFiledNameStrategy();
        initIObjectStrategy();
        initIChannelHandlerStrategy();
        Notification notification = new Notification();

        List<IObject> result = new ArrayList<>();

        IObject testObject = mock(IObject.class);

        IObject environment = new DSObject();
        IObject message = mock(IObject.class);
        IObject request = mock(IObject.class);
        environment.setValue(new FieldName("message"), message);
        environment.setValue(new FieldName("request"), request);

        when(testObject.getValue(new FieldName("chain"))).thenReturn(this.chain);
        when(testObject.getValue(new FieldName("callback"))).thenReturn(this.callback);
        when(testObject.getValue(new FieldName("environment"))).thenReturn(environment);

        doAnswer(invocationOnMock -> {
            result.add((IObject) invocationOnMock.getArguments()[0]);
            notification.setExecuted(true);
            return null;
        }).when(handler).handle(environment, this.chain, this.callback);
        source.setSource(testObject);
        IAsyncService endpoint = new TestHttpEndpoint(source, ScopeProvider.getCurrentScope(), handler, 0L, null);
        endpoint.start();
        while(true) {
            if(notification.getExecuted()) {
                break;
            }
        }
        endpoint.stop();
        verify(handler, times(1)).handle(environment, this.chain, this.callback);
        assertEquals(result.size(), 1);
        IObject resultMessage = (IObject) result.get(0).getValue(new FieldName("message"));
        IObject resultContext = (IObject) result.get(0).getValue(new FieldName("context"));
        assertSame(resultMessage, message);
        assertNotNull(resultContext);
        assertNotNull(resultContext.getValue(new FieldName("headers")));
        assertNotNull(resultContext.getValue(new FieldName("cookies")));
        assertNotNull(resultContext.getValue(new FieldName("request")));
        assertNotNull(resultContext.getValue(new FieldName("channel")));
    }

    @Test
    public void checkCorrectStopService()
            throws Exception {
        IEnvironmentHandler handler = mock(IEnvironmentHandler.class);
        ISource<IObject, IObject> source = new IObjectDataSource();
        initFiledNameStrategy();
        initIObjectStrategy();
        initIChannelHandlerStrategy();

        Notification notification = new Notification();
        IObject testObject = mock(IObject.class);
        source.setSource(testObject);
        IAsyncService endpoint = new TestHttpEndpoint(source, ScopeProvider.getCurrentScope(), handler, 0L, (a) -> {
            notification.setExecuted(true);
            while(true) {
            }
        });
        endpoint.start();
        while(true) {
            if(notification.getExecuted()) {
                break;
            }
        }
        endpoint.stop();
    }

    @Test
    public void checkAdditionNewDataToQueueAfterStart()
            throws Exception {
        IEnvironmentHandler handler = mock(IEnvironmentHandler.class);
        ISource<IObject, IObject> source = new IObjectDataSource();
        initFiledNameStrategy();
        initIObjectStrategy();
        initIChannelHandlerStrategy();
        Notification notification = new Notification();

        List<IObject> result = new ArrayList<>();

        IObject testObject = mock(IObject.class);
        IObject testAnotherObject = mock(IObject.class);
        IObject testEnvironment = new DSObject();
        IObject testAnotherEnvironment = new DSObject();

        IObject messageOfTestObject = mock(IObject.class);
        IObject messageOfAnotherTestObject = mock(IObject.class);
        IObject requestOfTestObject = mock(IObject.class);
        IObject requestOfAnotherTestObject = mock(IObject.class);

        testEnvironment.setValue(new FieldName("message"), messageOfTestObject);
        testEnvironment.setValue(new FieldName("request"), requestOfTestObject);
        testAnotherEnvironment.setValue(new FieldName("message"), messageOfAnotherTestObject);
        testAnotherEnvironment.setValue(new FieldName("request"), requestOfAnotherTestObject);

        doAnswer(invocationOnMock -> {
            result.add((IObject) invocationOnMock.getArguments()[0]);
            notification.setExecuted(true);
            return null;
        }).when(handler).handle(any(IObject.class), any(IReceiverChain.class), any(null));
        when(testObject.getValue(new FieldName("environment"))).thenReturn(testEnvironment);
        when(testAnotherObject.getValue(new FieldName("environment"))).thenReturn(testAnotherEnvironment);
        source.setSource(testObject);
        IAsyncService endpoint = new TestHttpEndpoint(source, ScopeProvider.getCurrentScope(), handler, 0L, null);
        endpoint.start();
        while(true) {
            if(notification.getExecuted()) {
                break;
            }
        }
        notification.setExecuted(false);
        source.setSource(testAnotherObject);
        while(true) {
            if(notification.getExecuted()) {
                break;
            }
        }
        endpoint.stop();
        verify(handler, times(2)).handle(any(IObject.class), any(IReceiverChain.class), any(null));
        assertEquals(result.size(), 2);
        IObject message1 = (IObject) result.get(0).getValue(new FieldName("message"));
        IObject context1 = (IObject) result.get(0).getValue(new FieldName("context"));
        IObject message2 = (IObject) result.get(1).getValue(new FieldName("message"));
        IObject context2 = (IObject) result.get(1).getValue(new FieldName("context"));
        assertSame(message1, messageOfTestObject);
        assertNotNull(context1);
        assertSame(message2, messageOfAnotherTestObject);
        assertNotNull(context2);
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnNullQueue()
            throws Exception {
        IEnvironmentHandler handler = mock(IEnvironmentHandler.class);
        IAsyncService endpoint = new TestHttpEndpoint(null, ScopeProvider.getCurrentScope(), handler, 0L, null);
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnNullScope()
            throws Exception {
        IEnvironmentHandler handler = mock(IEnvironmentHandler.class);
        ISource<IObject, IObject> source = mock(ISource.class);
        IAsyncService endpoint = new TestHttpEndpoint(source, null, handler, 0L, null);
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnNullHandler()
            throws Exception {
        ISource<IObject, IObject> source = mock(ISource.class);
        IAsyncService endpoint = new TestHttpEndpoint(source, ScopeProvider.getCurrentScope(), null, 0L, null);
        fail();
    }

    @Test
    public void checkSkipMessageOnExceptionInFutureBlock()
            throws Exception {
        initFiledNameStrategy();
        initIObjectStrategy();
        initIChannelHandlerStrategy();
        IEnvironmentHandler handler = mock(IEnvironmentHandler.class);
        ISource<IObject, IObject> source = new IObjectDataSource();
        Notification notification = new Notification();

        IObject testObject = mock(IObject.class);
        IObject testAnotherObject = mock(IObject.class);
        IObject testEnvironment = new DSObject();
        IObject testAnotherEnvironment = new DSObject();

        IObject messageOfTestObject = mock(IObject.class);
        IObject messageOfAnotherTestObject = mock(IObject.class);
        IObject requestOfTestObject = mock(IObject.class);
        IObject requestOfAnotherTestObject = mock(IObject.class);

        testEnvironment.setValue(new FieldName("message"), messageOfTestObject);
        testEnvironment.setValue(new FieldName("request"), requestOfTestObject);
        testAnotherEnvironment.setValue(new FieldName("message"), messageOfAnotherTestObject);
        testAnotherEnvironment.setValue(new FieldName("request"), requestOfAnotherTestObject);

        List<IObject> result = new ArrayList<>();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                IObject obj = (IObject) invocationOnMock.getArguments()[0];
                if (obj == testEnvironment) {
                    throw new Exception();
                }
                if (obj == testAnotherEnvironment) {
                    result.add(obj);
                }
                notification.setExecuted(true);
                return null;
            }
        }).when(handler).handle(any(IObject.class), any(IReceiverChain.class), any(null));
        when(testObject.getValue(new FieldName("environment"))).thenReturn(testEnvironment);
        when(testObject.getValue(new FieldName("chain"))).thenReturn(this.chain);
        when(testObject.getValue(new FieldName("callback"))).thenReturn(this.callback);
        when(testAnotherObject.getValue(new FieldName("environment"))).thenReturn(testAnotherEnvironment);
        when(testAnotherObject.getValue(new FieldName("chain"))).thenReturn(this.chain);
        when(testAnotherObject.getValue(new FieldName("callback"))).thenReturn(this.callback);
        source.setSource(testObject);
        IAsyncService endpoint = new TestHttpEndpoint(source, ScopeProvider.getCurrentScope(), handler, 0L, null);
        endpoint.start();
        source.setSource(testAnotherObject);
        while (true) {
            if (notification.getExecuted()) {
                break;
            }
        }
        endpoint.stop();
        assertEquals(result.size(), 1);
        assertSame(result.get(0), testAnotherEnvironment);
    }

    @Test
    public void checkSkipMessageOnExceptionInDefaultStrategyBlock()
            throws Exception {
        IEnvironmentHandler handler = mock(IEnvironmentHandler.class);
        ISource<IObject, IObject> source = new IObjectDataSource();
        Notification notification = new Notification();

        IObject testObject = mock(IObject.class);
        IObject testAnotherObject = mock(IObject.class);
        IObject testEnvironment = new DSObject();
        IObject testAnotherEnvironment = new DSObject();

        IObject messageOfTestObject = mock(IObject.class);
        IObject messageOfAnotherTestObject = mock(IObject.class);
        IObject requestOfTestObject = mock(IObject.class);
        IObject requestOfAnotherTestObject = mock(IObject.class);

        testEnvironment.setValue(new FieldName("message"), messageOfTestObject);
        testEnvironment.setValue(new FieldName("request"), requestOfTestObject);
        testAnotherEnvironment.setValue(new FieldName("message"), messageOfAnotherTestObject);
        testAnotherEnvironment.setValue(new FieldName("request"), requestOfAnotherTestObject);

        when(testObject.getValue(new FieldName("chain"))).thenReturn(this.chain);
        when(testObject.getValue(new FieldName("callback"))).thenReturn(this.callback);
        when(testObject.getValue(new FieldName("environment"))).thenReturn(testEnvironment);
        when(testAnotherObject.getValue(new FieldName("chain"))).thenReturn(this.chain);
        when(testAnotherObject.getValue(new FieldName("callback"))).thenReturn(this.callback);
        when(testAnotherObject.getValue(new FieldName("environment"))).thenReturn(testAnotherEnvironment);

        initFiledNameStrategy();
        initIObjectStrategy();
        IChannelHandler channelHandler = mock(IChannelHandler.class);
        IResolveDependencyStrategy strategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), TestChannelHandler.class.getCanonicalName()),
                strategy
        );
        when(strategy.resolve()).thenThrow(ResolutionException.class).thenReturn(channelHandler);
        List<IObject> result = new ArrayList<>();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                IObject obj = (IObject) invocationOnMock.getArguments()[0];
                result.add(obj);
                notification.setExecuted(true);

                return null;
            }
        }).when(handler).handle(any(IObject.class), any(IReceiverChain.class), any(null));
        source.setSource(testObject);
        IAsyncService endpoint = new TestHttpEndpoint(source, ScopeProvider.getCurrentScope(), handler, 1000L, null);
        endpoint.start();
        notification.setExecuted(false);
        source.setSource(testAnotherObject);
        while (true) {
            if (notification.getExecuted()) {
                break;
            }
        }
        endpoint.stop();
        assertSame(result.get(0).getValue(new FieldName("message")), messageOfAnotherTestObject);
    }

    private void initFiledNameStrategy()
            throws Exception {
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                new ResolveByNameIocStrategy((a)-> {
                    try {
                        return new FieldName((String) a[0]);
                    } catch (Exception e) {
                        throw new RuntimeException("Could not create new instance of FieldName.");
                    }
                })
        );
    }

    private void initIObjectStrategy()
            throws Exception {
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.getCanonicalName()),
                new ApplyFunctionToArgumentsStrategy((a)-> {
                    try {
                        return new DSObject();
                    } catch (Exception e) {
                        throw new RuntimeException("Could not create new instance of FieldName.");
                    }
                })
        );
    }

    private void initIChannelHandlerStrategy()
            throws Exception {
        IChannelHandler channelHandler = mock(IChannelHandler.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), TestChannelHandler.class.getCanonicalName()),
                new SingletonStrategy(channelHandler)
        );
    }
}

class Notification {
    private volatile boolean executed = false;

    public boolean getExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }
}