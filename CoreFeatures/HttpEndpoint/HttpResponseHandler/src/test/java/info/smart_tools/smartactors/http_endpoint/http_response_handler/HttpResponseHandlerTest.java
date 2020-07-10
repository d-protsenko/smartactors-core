package info.smart_tools.smartactors.http_endpoint.http_response_handler;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.class_management.module_manager.ModuleManager;
import info.smart_tools.smartactors.endpoint.interfaces.ideserialize_strategy.IDeserializeStrategy;
import info.smart_tools.smartactors.endpoint.interfaces.ideserialize_strategy.exceptions.DeserializationException;
import info.smart_tools.smartactors.endpoint.interfaces.iresponse_handler.exception.ResponseHandlerException;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Created by sevenbits on 30.09.16.
 */
public class HttpResponseHandlerTest {
    IQueue<ITask> taskQueue;
    Object receiverChain;
    HttpResponseHandler responseHandler;
    IMessageProcessingSequence messageProcessingSequence;
    IMessageProcessor messageProcessor;
    FullHttpResponse response;
    IDeserializeStrategy strategy;
    HttpHeaders headers;
    ChannelHandlerContext ctx;
    Object mapId;
    IChainStorage chainStorage;

    @Before
    public void setUp() throws ScopeProviderException, RegistrationException, ResolutionException, InvalidArgumentException, ResponseHandlerException {
        this.mapId = mock(Object.class);
        this.chainStorage = mock(IChainStorage.class);
        this.taskQueue = mock(IQueue.class);
        this.receiverChain = mock(Object.class);
        this.messageProcessingSequence = mock(IMessageProcessingSequence.class);
        this.messageProcessor = mock(IMessageProcessor.class);
        this.response = mock(FullHttpResponse.class);
        this.strategy = mock(IDeserializeStrategy.class);
        this.ctx = mock(ChannelHandlerContext.class);
        this.headers = mock(HttpHeaders.class);
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
                IOC.getKeyForKeyByNameStrategy(),
                new ResolveByNameIocStrategy()
        );
        IOC.register(
                Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            try {
                                return new FieldName((String) args[0]);
                            } catch (InvalidArgumentException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
        );
        IOC.register(
                Keys.getKeyByName("info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence"),
                new SingletonStrategy(
                        messageProcessingSequence
                )
        );
        IOC.register(
                Keys.getKeyByName("info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor"),
                new SingletonStrategy(
                        messageProcessor
                )
        );
        IOC.register(
                Keys.getKeyByName("httpResponseResolver"),
                new SingletonStrategy(
                        strategy
                )
        );
        IOC.register(
                Keys.getKeyByName("EmptyIObject"),
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> new DSObject()

                )
        );
        Object obj = mock(Object.class);
        IOC.register(Keys.getKeyByName("cancelTimerOnRequest"),
                new SingletonStrategy(obj));

        IOC.register(
                Keys.getKeyByName("chain_id_from_map_name_and_message"),
                new SingletonStrategy(
                        mapId
                )
        );
        IOC.register(
                Keys.getKeyByName(IChainStorage.class.getCanonicalName()),
                new SingletonStrategy(
                        chainStorage
                )
        );
        this.responseHandler = new HttpResponseHandler(
                taskQueue,
                5,
                receiverChain,
                new DSObject("{" +
                        "\"uuid\": \"uuid\", " +
                        "\"messageMapId\": \"messageMapId\", " +
                        "\"message\": {}, " +
                        "\"method\": \"POST\", " +
                        "\"uri\": \"https://foo.bar\"" +
                        "}"),
                ScopeProvider.getCurrentScope(),
                ModuleManager.getCurrentModule()
        );
    }

    @Test
    public void testNewTaskAddedToQueue()
            throws InvalidArgumentException, DeserializationException, InterruptedException, ResponseHandlerException {
        String chainName = "chainName";
        when(response.headers()).thenReturn(headers);
        when(headers.get("messageMapId")).thenReturn("messageMap");
        when(strategy.deserialize(any())).thenReturn(new DSObject("{\"foo\": \"bar\"}"));
        responseHandler.handle(ctx, response);
        verify(taskQueue, times(1)).put(any());
    }
}
