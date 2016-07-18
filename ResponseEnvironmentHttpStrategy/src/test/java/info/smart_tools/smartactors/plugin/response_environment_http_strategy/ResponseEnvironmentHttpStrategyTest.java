package info.smart_tools.smartactors.plugin.response_environment_http_strategy;

import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.iresponse.IResponse;
import info.smart_tools.smartactors.core.response.Response;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.DefaultCookie;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;


@RunWith(PowerMockRunner.class)
@PrepareForTest({CookieExtractor.class, HeadersExtractor.class})
public class ResponseEnvironmentHttpStrategyTest {
    @Test
    public void testCookieSetting() throws InvalidArgumentException, ResolutionException, ReadValueException {
        ResponseEnvironmentHttpStrategy strategy = new ResponseEnvironmentHttpStrategy();
        PowerMockito.mockStatic(CookieExtractor.class);
        List<Cookie> cookies = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            cookies.add(new DefaultCookie(String.valueOf(i), String.valueOf(i)));
        }
        BDDMockito.given(CookieExtractor.extract(any(IObject.class))).willReturn(cookies);
        IResponse response = new Response();
        strategy.setEnvironment(new DSObject("{\"foo\": \"bar\"}"), response);
        assertEquals(response.getEnvironment("cookies"), cookies);
    }

    @Test
    public void testHeadersSetting() throws InvalidArgumentException, ResolutionException, ReadValueException {
        ResponseEnvironmentHttpStrategy strategy = new ResponseEnvironmentHttpStrategy();
        PowerMockito.mockStatic(HeadersExtractor.class);
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("1", "1");
        headers.set("2", "2");
        BDDMockito.given(HeadersExtractor.extract(any(IObject.class))).willReturn(headers);
        IResponse response = new Response();
        strategy.setEnvironment(new DSObject("{\"foo\": \"bar\"}"), response);
        assertEquals(response.getEnvironment("headers"), headers);
    }

}
