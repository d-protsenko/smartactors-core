package info.smart_tools.smartactors.http_endpoint.http_client_initializer;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.endpoint.interfaces.iclient.IClient;
import info.smart_tools.smartactors.endpoint.interfaces.iresponse_handler.IResponseHandler;
import info.smart_tools.smartactors.http_endpoint.http_client.HttpClient;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;
import io.netty.handler.codec.http.HttpRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.requests.ClassRequest;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by sevenbits on 07.10.16.
 */
public class HttpClientInitializerTest {
    @Before
    public void setUp() throws ScopeProviderException, RegistrationException, ResolutionException, InvalidArgumentException {
        ScopeProvider.subscribeOnCreationNewScope(
                scope -> {
                    try {
                        scope.setValue(IOC.getIocKey(), new StrategyContainer());
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }
        );

        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope mainScope = ScopeProvider.getScope(keyOfMainScope);
        ScopeProvider.setCurrentScope(mainScope);

        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy()
        );
        IOC.register(Keys.getOrAdd(IFieldName.class.getCanonicalName()), new CreateNewInstanceStrategy(
                (args) -> {
                    try {
                        return new FieldName((String) args[0]);
                    } catch (InvalidArgumentException e) {
                        throw new RuntimeException(e);
                    }
                }
        ));
    }

    @Test
    public void testAdditionOfErrorOnConvertRequestToTimeoutRequest() throws ResolutionException, RegistrationException, InvalidArgumentException, ReadValueException {
        HttpClientInitializer.init();
        IFieldName errorFieldName = new FieldName("error");
        IObject request = new DSObject("{\"message\": \"Hello, world\", \"uri\": \"http://foo.bar\", \"method\": \"POST\"}");
        IObject timeoutRequest = IOC.resolve(Keys.getOrAdd("convert_request_to_timeout_request"), request);
        assertEquals(timeoutRequest.getValue(errorFieldName), "Time limit exceeded");
    }

    @Test
    public void testRemovingMessageOnConvertRequestToTimeoutRequest() throws ResolutionException, RegistrationException, InvalidArgumentException, ReadValueException {
        HttpClientInitializer.init();
        IFieldName messageFieldName = new FieldName("message");
        IObject request = new DSObject("{\"message\": \"Hello, world\", \"uri\": \"http://foo.bar\", \"method\": \"POST\"}");
        IObject timeoutRequest = IOC.resolve(Keys.getOrAdd("convert_request_to_timeout_request"), request);
        assertEquals(timeoutRequest.getValue(messageFieldName), null);
    }

    @Test
    public void testSendingTimeoutResponseTask() throws ResolutionException, RegistrationException,
            InvalidArgumentException, TaskExecutionException {
        HttpClientInitializer.init();
        IObject request = new DSObject("{\"error\": \"Time limit exceeded\", \"uri\": \"http://foo.bar\", \"method\": \"POST\"}");
        IOC.register(Keys.getOrAdd("convert_request_to_timeout_request"), new SingletonStrategy(request));
        IResponseHandler responseHandler = mock(IResponseHandler.class);
        IOC.register(Keys.getOrAdd(IResponseHandler.class.getCanonicalName()), new SingletonStrategy(responseHandler));
        HttpClient httpClient = mock(HttpClient.class);
        IOC.register(Keys.getOrAdd(HttpClient.class.getCanonicalName()), new SingletonStrategy(httpClient));
        CompletableFuture completableFuture = mock(CompletableFuture.class);
        when(httpClient.start()).thenReturn(completableFuture);
        when(completableFuture.thenAccept(any())).thenCallRealMethod();
        ITask task = IOC.resolve(Keys.getOrAdd("send_timeout_response_task"), request);
        task.execute();
        verify(httpClient.start(), timeout(1000));
        verify(httpClient.send(any()), timeout(2000));
    }
}
