package info.smart_tools.smaractors.http_endopint.response_content_chunked_json_strategy;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.endpoint.interfaces.iresponse.IResponse;
import info.smart_tools.smartactors.http_endpoint.response_content_chunked_json_strategy.ResponseContentChunkedJsonStrategy;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.SerializeException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by sevenbits on 28.10.16.
 */
public class ResponseContentChunkedJsonStrategyTest {
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
    }

    @Test
    public void testStartOfTheChunkedMessage() throws ResolutionException, InvalidArgumentException, RegistrationException, ReadValueException, SerializeException {
        ResponseContentChunkedJsonStrategy jsonStrategy = new ResponseContentChunkedJsonStrategy();
        IFieldName chunkedFieldName = mock(IFieldName.class);
        IOC.register(
                Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> chunkedFieldName
                )
        );
        IResponse responseObject = mock(IResponse.class);
        IObject response = mock(IObject.class);
        String content = "{\"hello\": \"world\"}";
        when(response.getValue(chunkedFieldName)).thenReturn("start");
        when(response.serialize()).thenReturn(content);
        jsonStrategy.setContent(response, responseObject);
        verify(responseObject).setContent((17 + "\r\n" + content.substring(0, content.length() - 1) + "\r\n").getBytes(Charset.forName("UTF-8")));
    }

    @Test
    public void testEndOfTheChunkedMessage() throws ResolutionException, InvalidArgumentException, RegistrationException, ReadValueException, SerializeException {
        ResponseContentChunkedJsonStrategy jsonStrategy = new ResponseContentChunkedJsonStrategy();
        IFieldName chunkedFieldName = mock(IFieldName.class);
        IOC.register(
                Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> chunkedFieldName
                )
        );
        IResponse responseObject = mock(IResponse.class);
        IObject response = mock(IObject.class);
        String content = "{\"hello\": \"world\"}";
        when(response.getValue(chunkedFieldName)).thenReturn("end");
        when(response.serialize()).thenReturn(content);
        jsonStrategy.setContent(response, responseObject);
        verify(responseObject).setContent((17 + "\r\n" + content.substring(1, content.length()) + "\r\n").getBytes(Charset.forName("UTF-8")));
    }


    @Test
    public void testMiddleOfTheChunkedMessage() throws ResolutionException, InvalidArgumentException, RegistrationException, ReadValueException, SerializeException {
        ResponseContentChunkedJsonStrategy jsonStrategy = new ResponseContentChunkedJsonStrategy();
        IFieldName chunkedFieldName = mock(IFieldName.class);
        IOC.register(
                Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> chunkedFieldName
                )
        );
        IResponse responseObject = mock(IResponse.class);
        IObject response = mock(IObject.class);
        String content = "{\"hello\": \"world\"}";
        when(response.getValue(chunkedFieldName)).thenReturn("continue");
        when(response.serialize()).thenReturn(content);
        jsonStrategy.setContent(response, responseObject);
        verify(responseObject).setContent((16 + "\r\n" + content.substring(1, content.length()-1) + "\r\n").getBytes(Charset.forName("UTF-8")));
    }

}
