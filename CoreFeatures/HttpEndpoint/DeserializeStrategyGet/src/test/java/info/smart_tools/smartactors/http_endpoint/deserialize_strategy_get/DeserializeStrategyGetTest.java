package info.smart_tools.smartactors.http_endpoint.deserialize_strategy_get;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.endpoint.interfaces.iadd_request_parameters_to_iobject.exception.AddRequestParametersToIObjectException;
import info.smart_tools.smartactors.endpoint.interfaces.ideserialize_strategy.exceptions.DeserializationException;
import info.smart_tools.smartactors.http_endpoint.deserialize_strategy_get.parse_tree.IParseTree;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class DeserializeStrategyGetTest {


    FullHttpRequest httpRequest;
    IObject emptyIObject;
    IParseTree parseTree;
    QueryStringDecoder decoder;

    @Before
    public void setUp()
            throws ScopeProviderException, RegistrationException, ResolutionException, InvalidArgumentException {

        emptyIObject = mock(IObject.class);
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
                args -> {
                    try {
                        return new FieldName((String) args[0]);
                    } catch (InvalidArgumentException e) {
                        throw new RuntimeException(e);
                    }
                }
        ));

        decoder = mock(QueryStringDecoder.class);
        IOC.register(
                Keys.getKeyByName(QueryStringDecoder.class.getCanonicalName()),
                new SingletonStrategy(
                        decoder
                )
        );

        parseTree = mock(IParseTree.class);
        IOC.register(
                Keys.getKeyByName(IParseTree.class.getCanonicalName()),
                new SingletonStrategy(
                        parseTree
                )
        );
        IOC.register(
                Keys.getKeyByName("EmptyIObject"),
                new ApplyFunctionToArgumentsStrategy(
                        args -> new DSObject()
                )
        );

    }

    @Test
    public void testUriWithEmptyArgs()
            throws DeserializationException,
            ResolutionException,
            InvalidArgumentException,
            RegistrationException,
            ChangeValueException,
            AddRequestParametersToIObjectException {
        IOC.register(
                Keys.getKeyByName("EmptyIObject"),
                new SingletonStrategy(
                        emptyIObject
                )
        );

        IFieldName fieldName = new FieldName("messageMapId");

        String arg = "/hello";
        when(decoder.uri()).thenReturn(arg);
        when(decoder.parameters()).thenReturn(new HashMap<>());

        Map<String, String> outputMap = new HashMap<>();
        outputMap.put("messageMapId", "hello");
        when(parseTree.match(arg)).thenReturn(outputMap);

        String testUri = "www.www.ru/hello";
        httpRequest = mock(FullHttpRequest.class);
        when(httpRequest.uri()).thenReturn(testUri);
        DeserializeStrategyGet deserializeStrategyGet = new DeserializeStrategyGet(new ArrayList<>());
        deserializeStrategyGet.extract(emptyIObject, httpRequest);
        verify(emptyIObject).setValue(fieldName, "hello");
    }

    @Test
    public void testUriWithArgs()
            throws DeserializationException,
            InvalidArgumentException,
            ReadValueException, ResolutionException,
            RegistrationException,
            AddRequestParametersToIObjectException {

        String testUri = "www.www.ru/hello?hello=world";
        httpRequest = mock(FullHttpRequest.class);
        when(httpRequest.uri()).thenReturn(testUri);

        String arg = "/hello";
        when(decoder.uri()).thenReturn(arg);
        Map<String, List<String>> parameters = new HashMap<>();
        parameters.put("hello", Collections.singletonList("world"));
        when(decoder.parameters()).thenReturn(parameters);

        Map<String, String> outputMap = new HashMap<>();
        outputMap.put("messageMapId", "hello");
        when(parseTree.match(arg)).thenReturn(outputMap);

        DeserializeStrategyGet deserializeStrategyGet = new DeserializeStrategyGet(new ArrayList<>());
        IObject iObject = new DSObject();
        deserializeStrategyGet.extract(iObject, httpRequest);
        assertEquals((iObject.getValue(new FieldName("hello"))), "world");
        assertEquals(iObject.getValue(new FieldName("messageMapId")), "hello");
    }

    @Test
    public void testUriWithoutArgs()
            throws DeserializationException,
            InvalidArgumentException,
            ReadValueException, ResolutionException, RegistrationException, AddRequestParametersToIObjectException {
        IOC.register(Keys.getKeyByName("EmptyIObject"), new SingletonStrategy(
                        emptyIObject
                )
        );
        String testUri = "www.www.ru";
        httpRequest = mock(FullHttpRequest.class);
        when(httpRequest.uri()).thenReturn(testUri);
        DeserializeStrategyGet deserializeStrategyGet = new DeserializeStrategyGet(new ArrayList<>());
        IObject iObject = emptyIObject;
        deserializeStrategyGet.extract(iObject, httpRequest);
        assertEquals(iObject, this.emptyIObject);
    }
}
