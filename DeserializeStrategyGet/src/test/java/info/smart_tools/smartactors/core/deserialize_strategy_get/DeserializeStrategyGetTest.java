package info.smart_tools.smartactors.core.deserialize_strategy_get;

import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.deserialize_strategy_get.parse_tree.IParseTree;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.exceptions.DeserializationException;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeserializeStrategyGetTest {


    FullHttpRequest httpRequest;
    IObject emptyIObject;
    IParseTree parseTree;
    QueryStringDecoder decoder;

    @Before
    public void setUp() throws ScopeProviderException, RegistrationException, ResolutionException, InvalidArgumentException {

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
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy()
        );


        IOC.register(Keys.getOrAdd(IFieldName.class.getCanonicalName()), new CreateNewInstanceStrategy(
                args -> {
                    try {
                        return new FieldName((String) args[0]);
                    } catch (InvalidArgumentException e) {
                        throw new RuntimeException(e);
                    }
                }
        ));

        decoder = mock(QueryStringDecoder.class);
        IOC.register(Keys.getOrAdd(QueryStringDecoder.class.getCanonicalName()), new SingletonStrategy(
                        decoder
                )
        );

        parseTree = mock(IParseTree.class);
        IOC.register(Keys.getOrAdd(IParseTree.class.getCanonicalName()), new SingletonStrategy(
                        parseTree
                )
        );
        IOC.register(Keys.getOrAdd("EmptyIObject"), new CreateNewInstanceStrategy(
                        args -> new DSObject()
                )
        );

    }

    @Test
    public void testUriWithEmptyArgs() throws DeserializationException, ResolutionException, InvalidArgumentException, RegistrationException, ChangeValueException {
        IOC.register(Keys.getOrAdd("EmptyIObject"), new SingletonStrategy(
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
        IObject iObject = deserializeStrategyGet.deserialize(httpRequest);
        verify(emptyIObject).setValue(fieldName, "hello");
    }

    @Test
    public void testUriWithArgs() throws DeserializationException, InvalidArgumentException, ReadValueException, ResolutionException, RegistrationException {

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
        IObject iObject = deserializeStrategyGet.deserialize(httpRequest);
        assertEquals(((List<String>) iObject.getValue(new FieldName("hello"))).get(0), "world");
        assertEquals(iObject.getValue(new FieldName("messageMapId")), "hello");
    }

    @Test
    public void testUriWithoutArgs() throws DeserializationException, InvalidArgumentException, ReadValueException, ResolutionException, RegistrationException {
        IOC.register(Keys.getOrAdd("EmptyIObject"), new SingletonStrategy(
                        emptyIObject
                )
        );
        String testUri = "www.www.ru";
        httpRequest = mock(FullHttpRequest.class);
        when(httpRequest.uri()).thenReturn(testUri);
        DeserializeStrategyGet deserializeStrategyGet = new DeserializeStrategyGet(new ArrayList<>());
        IObject iObject = deserializeStrategyGet.deserialize(httpRequest);
        assertEquals(iObject, this.emptyIObject);
    }
}