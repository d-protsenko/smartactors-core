package info.smart_tools.smartactors.core.http_response_sender;


import info.smart_tools.smartactors.core.IMessageMapper;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iresponse.IResponse;
import info.smart_tools.smartactors.core.iresponse_sender.IResponseSender;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.util.Map;
import java.util.Set;

/**
 * Exchange object for received HTTP request.
 * It sends a response to the request and closes the connection.
 */
public class HttpResponseSender implements IResponseSender {
    /**
     * Constructor for http response sender
     */
    public HttpResponseSender() {
    }

    @Override
    public void send(final IResponse responseObject,
                     final ChannelHandlerContext ctx) {
        FullHttpResponse response =
                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, getResponseStatus(responseObject),
                        Unpooled.wrappedBuffer(responseObject.getBody().getBytes()));
        setHeaders(responseObject, response);
        setCookies(responseObject, response);
        ctx.writeAndFlush(response);
    }

    private HttpResponseStatus getResponseStatus(final IResponse response) {
        if (null != response.getEnvironment("statusCode")) {
            return HttpResponseStatus.valueOf((Integer) response.getEnvironment("statusCode"));
        }
        return HttpResponseStatus.OK;
    }

    private void setHeaders(final IResponse responseObject, FullHttpResponse response) {
        Map<String, Object> responseHeaders = (Map<String, Object>) responseObject.getEnvironment("headers");
        if (null != responseHeaders) {
            Set<String> keysSet = responseHeaders.keySet();
            for (String key : keysSet) {
                response.headers().set(key, responseHeaders.get(key));
            }
        }
    }

    private void setCookies(final IResponse responseObject, FullHttpResponse response) {
        Map<String, String> cookies = (Map<String, String>) responseObject.getEnvironment("cookies");

        if (null != cookies) {
            Set<String> keysSet = cookies.keySet();
            for (String key : keysSet) {
                Cookie cookie = new DefaultCookie(
                        key, cookies.get(key));
                response.headers().set(HttpHeaders.Names.SET_COOKIE,
                        ServerCookieEncoder.encode(cookie));
            }
        }
    }
}
