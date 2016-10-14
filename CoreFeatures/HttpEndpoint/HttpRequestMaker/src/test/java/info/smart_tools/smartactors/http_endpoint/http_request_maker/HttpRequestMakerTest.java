package info.smart_tools.smartactors.http_endpoint.http_request_maker;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.endpoint.interfaces.imessage_mapper.IMessageMapper;
import info.smart_tools.smartactors.endpoint.irequest_maker.exception.RequestMakerException;
import info.smart_tools.smartactors.http_endpoint.message_to_bytes_mapper.MessageToBytesMapper;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by sevenbits on 14.10.16.
 */
public class HttpRequestMakerTest {
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
                )
        );
        IMessageMapper<byte[]> messageMapper = new MessageToBytesMapper();
        IOC.register(Keys.getOrAdd(MessageToBytesMapper.class.getCanonicalName()), new SingletonStrategy(
                        messageMapper
                )
        );
    }

    @Test
    public void testRequestMaking() throws InvalidArgumentException, ResolutionException, RequestMakerException {
        IObject requestIObject = new DSObject(
                "{" +
                        "\"uuid\": \"uuid\", " +
                        "\"uri\": \"http://foo.bar\", " +
                        "\"method\": \"POST\", " +
                        "\"timeout\": 10000, " +
                        "\"exceptionalMessageMapId\": \"SelectChain\"," +
                        "\"startChain\": \"SelectChain\"," +
                        "\"messageMapId\": \"sendRequest\", " +
                        "\"content\": {\"hello\": \"world\"}" +
                        "}"
        );
        HttpRequestMaker requestMaker = new HttpRequestMaker();
        FullHttpRequest httpRequest = requestMaker.make(requestIObject);
        assertEquals(httpRequest.method(), HttpMethod.POST);
        assertEquals(httpRequest.uri(), "");
        assertEquals(httpRequest.content(), Unpooled.copiedBuffer("{\"hello\":\"world\"}".getBytes()));
    }

    @Test
    public void testRequestMakingWithGoodURI() throws InvalidArgumentException, ResolutionException, RequestMakerException {
        IObject requestIObject = new DSObject(
                "{" +
                        "\"uuid\": \"uuid\", " +
                        "\"uri\": \"http://foo.bar/hello/world\", " +
                        "\"method\": \"POST\", " +
                        "\"timeout\": 10000, " +
                        "\"exceptionalMessageMapId\": \"SelectChain\"," +
                        "\"startChain\": \"SelectChain\"," +
                        "\"messageMapId\": \"sendRequest\", " +
                        "\"content\": {\"hello\": \"world\"}" +
                        "}"
        );
        HttpRequestMaker requestMaker = new HttpRequestMaker();
        FullHttpRequest httpRequest = requestMaker.make(requestIObject);
        assertEquals(httpRequest.uri(), "/hello/world");
    }

    @Test
    public void testCookieSetting() throws InvalidArgumentException, ResolutionException, RequestMakerException {
        IObject requestIObject = new DSObject(
                "{\n" +
                        "  \"uuid\": \"uuid\",\n" +
                        "  \"uri\": \"http://requestb.in/1fjytuf1\",\n" +
                        "  \"method\": \"POST\",\n" +
                        "  \"timeout\": 10000,\n" +
                        "  \"exceptionalMessageMapId\": \"SelectChain\",\n" +
                        "  \"startChain\": \"SelectChain\",\n" +
                        "  \"messageMapId\": \"sendRequest\",\n" +
                        "  \"content\": {\n" +
                        "    \"hello\": \"world\"\n" +
                        "  },\n" +
                        "  \"cookie\": [\n" +
                        "    {\n" +
                        "      \"name\": \"hello\",\n" +
                        "      \"value\": \"world\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"
        );
        HttpRequestMaker requestMaker = new HttpRequestMaker();
        FullHttpRequest httpRequest = requestMaker.make(requestIObject);
        assertEquals(httpRequest.headers().get(HttpHeaders.Names.COOKIE), "hello=world");
    }


    @Test
    public void testHeadersSetting() throws InvalidArgumentException, ResolutionException, RequestMakerException {
        IObject requestIObject = new DSObject(
                "{\n" +
                        "  \"uuid\": \"uuid\",\n" +
                        "  \"uri\": \"http://requestb.in/1fjytuf1\",\n" +
                        "  \"method\": \"POST\",\n" +
                        "  \"timeout\": 10000,\n" +
                        "  \"exceptionalMessageMapId\": \"SelectChain\",\n" +
                        "  \"startChain\": \"SelectChain\",\n" +
                        "  \"messageMapId\": \"sendRequest\",\n" +
                        "  \"content\": {\n" +
                        "    \"hello\": \"world\"\n" +
                        "  },\n" +
                        "  \"headers\": [\n" +
                        "    {\n" +
                        "      \"name\": \"hello\",\n" +
                        "      \"value\": \"world\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"name\": \"foo\",\n" +
                        "      \"value\": \"bar\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"
        );
        HttpRequestMaker requestMaker = new HttpRequestMaker();
        FullHttpRequest httpRequest = requestMaker.make(requestIObject);
        assertEquals(httpRequest.headers().get("hello"), "world");
        assertEquals(httpRequest.headers().get("foo"), "bar");
    }
}
