import com.google.common.base.Charsets;
import com.google.common.util.concurrent.Uninterruptibles;
import info.smart_tools.smartactors.core.*;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.ds_object.FieldName;
import info.smart_tools.smartactors.core.endpoint_handler.EndpointHandler;
import info.smart_tools.smartactors.core.http_server.HttpServer;
import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.message_processing.exceptions.MessageReceiveException;
import info.smart_tools.smartactors.core.message_processing_sequence.MessageProcessingSequence;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smat_tools.smartactors.core.iexchange.IExchange;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.junit.BeforeClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


public class HttpEndpointTest {
    protected IMessageMapper<byte[]> mapperStub;
    protected HttpServer endpoint;
    protected HttpClient client;
    protected IEnvironmentHandler environmentHandler;
    protected BiConsumer<ChannelHandlerContext, FullHttpResponse> handlerStub;
    protected IReceiverChain receiver;


    protected int getTestingPort() {
        return 9001;
    }


    @BeforeMethod
    public void setUp() throws ExecutionException, InterruptedException, URISyntaxException, InvalidArgumentException, ResolutionException, RegistrationException, ScopeProviderException {
        mapperStub = mock(IMessageMapper.class);
        receiver = mock(IReceiverChain.class);
        environmentHandler = mock(IEnvironmentHandler.class);
        HttpEndpointTest me = this;

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
        IKey keyMessageProcessingSequence = Keys.getOrAdd(MessageProcessingSequence.class.toString());
        IKey keyIObject = Keys.getOrAdd(IObject.class.toString());
        IKey keyIFieldName = Keys.getOrAdd(IFieldName.class.toString());
        IOC.register(
                keyMessageProcessingSequence,
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new MessageProcessingSequence((int) args[0], (IReceiverChain) args[1]);
                            } catch (InvalidArgumentException | ResolutionException ignored) {
                            }
                            return null;
                        }
                )
        );
        IOC.register(
                keyIObject,
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new DSObject((String) args[0]);
                            } catch (InvalidArgumentException ignored) {
                            }
                            return null;
                        }
                )
        );
        IOC.register(
                keyIFieldName,
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new FieldName((String) args[0]);
                            } catch (InvalidArgumentException ignored) {
                            }
                            return null;
                        }
                )
        );

        ChannelInboundHandler handler = new SimpleChannelInboundHandler<FullHttpResponse>(getResponseClass()) {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
                me.handlerStub.accept(ctx, msg);
            }
        };
        try {
            endpoint = createEndpoint(environmentHandler, receiver, mapperStub);
        } catch (ResolutionException e) {
            fail("Failed to create endpoint");
        }
        client = createClient(handler);
        endpoint.start().thenCompose(x -> client.start()).get();
    }

    @AfterMethod
    public void tearDown() throws ExecutionException, InterruptedException {
        CompletableFuture.allOf(endpoint.stop()).get();
    }

    @Test
    public void whenEndpointHandlerReceivesRequest_ItShouldHandleEnvironmentHandler() throws ResolutionException {
        IObject stubMessage = IOC.resolve(Keys.getOrAdd(IObject.class.toString()), "{\"hello\": \"world\"}");
        when(mapperStub.deserialize(any(byte[].class))).thenReturn(stubMessage);
        HttpRequest request = createTestRequest();
        sendRequest(request);
        verify(environmentHandler, timeout(1000)).handle(any(IObject.class), any(IReceiverChain.class));
    }

    protected CompletableFuture<Void> sendRequest(HttpRequest request) {
        return client.send(request);
    }

    protected HttpServer createEndpoint(
            IEnvironmentHandler environmentHandler, IReceiverChain receiver, IMessageMapper<byte[]> mapper
    ) throws ResolutionException, ScopeProviderException {
        return new HttpEndpoint(getTestingPort(), ScopeProvider.getCurrentScope(),
                environmentHandler, receiver, mapper, new DeserializeStrategyPostJson(mapper));
    }

    protected HttpClient createClient(ChannelInboundHandler handler) throws URISyntaxException {
        return new HttpClient(new URI("http://localhost:" + getTestingPort()), handler);
    }

    protected Class<? extends FullHttpResponse> getResponseClass() {
        return FullHttpResponse.class;
    }

    protected HttpRequest createTestRequest() {
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "http://localhost:" + getTestingPort());
        request.headers().set(HttpHeaders.Names.HOST, "localhost");
        request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
        request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);

        return request;
    }
}
