package info.smart_tools.smartactors.http_endpoint.cookies_setter;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.http_endpoint.interfaces.icookies_extractor.exceptions.CookieSettingException;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import io.netty.handler.codec.http.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CookiesSetterTest {
    IField field;

    @Before
    public void setUp() throws ScopeProviderException, RegistrationException, ResolutionException, InvalidArgumentException {
        field = mock(IField.class);
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
                IOC.getKeyForKeyByNameResolveStrategy(),
                new ResolveByNameIocStrategy()
        );

        IKey keyFieldName = Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName");
        IOC.register(
                keyFieldName,
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
        IKey keyIField = Keys.getOrAdd(IField.class.getCanonicalName());
        IOC.register(
                keyIField,
                new SingletonStrategy(field)
        );

    }

    @Test
    public void testCookiesSetting() throws InvalidArgumentException, CookieSettingException, ReadValueException {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        CookiesSetter setter = new CookiesSetter();
        IObject environment = new DSObject("{\n" +
                "  \"context\": {\n" +
                "    \"cookies\": [\n" +
                "      {\n" +
                "        \"name\": \"foo\", " +
                "        \"value\": \"bar\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\": \"hello\",  " +
                "        \"value\": \"world\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}");
        IObject context = new DSObject(
                " {" +
                        "    \"cookies\": [\n" +
                        "      {\n" +
                        "        \"name\": \"foo\", " +
                        "        \"value\": \"bar\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"name\": \"hello\",  " +
                        "        \"value\": \"world\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  }\n");
        IObject cookie1 = new DSObject("{\n" +
                "        \"name\": \"foo\", " +
                "        \"value\": \"bar\"\n" +
                "      }");
        IObject cookie2 = new DSObject("{\n" +
                "        \"name\": \"hello\", " +
                "        \"value\": \"world\"\n" +
                "      }");
        List<String> cookiesGoodString = new ArrayList<>(2);
        cookiesGoodString.add("foo=bar");
        cookiesGoodString.add("hello=world");

        List<IObject> cookies = new ArrayList<>(2);
        cookies.add(cookie1);
        cookies.add(cookie2);
        when(field.in(environment, IObject.class)).thenReturn(context);
        when(field.in(context, List.class)).thenReturn(cookies);
        setter.set(response, environment);
        List<String> cookiesString = response.headers().getAll(HttpHeaders.Names.SET_COOKIE);
        for (String cookie : cookiesString) {
            assertEquals(cookiesGoodString.get(0), cookiesString.get(0));
        }
    }

    @Test
    public void testCookiesSettingWithTime_ShouldSetDiscard()
            throws InvalidArgumentException, CookieSettingException, ReadValueException {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        CookiesSetter setter = new CookiesSetter();
        IObject environment = new DSObject("{\n" +
                "  \"context\": {\n" +
                "    \"cookies\": [\n" +
                "      {\n" +
                "        \"name\": \"foo\", " +
                "        \"value\": \"bar\",\n" +
                "         \"maxAge\": 12 \n" +
                "      },\n" +
                "      {\n" +
                "        \"name\": \"hello\",  " +
                "        \"value\": \"world\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}");
        IObject context = new DSObject(
                " {" +
                        "    \"cookies\": [\n" +
                        "      {\n" +
                        "        \"name\": \"foo\", " +
                        "        \"value\": \"bar\",\n" +
                        "         \"maxAge\": 12 \n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"name\": \"hello\",  " +
                        "        \"value\": \"world\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  }\n");
        IObject cookie1 = new DSObject("{\n" +
                "        \"name\": \"foo\", " +
                "        \"value\": \"bar\",\n" +
                "         \"maxAge\": 12 \n" +
                "      }");
        IObject cookie2 = new DSObject("{\n" +
                "        \"name\": \"hello\", " +
                "        \"value\": \"world\"\n" +
                "      }");
        List<IObject> cookies = new ArrayList<>(2);
        cookies.add(cookie1);
        cookies.add(cookie2);
        when(field.in(environment, IObject.class)).thenReturn(context);
        when(field.in(context, List.class)).thenReturn(cookies);
        setter.set(response, environment);
        List<String> cookiesString = response.headers().getAll(HttpHeaders.Names.SET_COOKIE);
        assertTrue(cookiesString.get(0).lastIndexOf("Expires")>0);
        assertFalse(cookiesString.get(1).lastIndexOf("Expires")>0);
    }
}
