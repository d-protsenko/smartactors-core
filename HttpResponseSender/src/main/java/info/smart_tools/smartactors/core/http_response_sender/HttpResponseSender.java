package info.smart_tools.smartactors.core.http_response_sender;

import info.smart_tools.smartactors.core.iresponse.IResponse;
import info.smart_tools.smartactors.core.iresponse_sender.IResponseSender;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.ServerCookieEncoder;


import java.util.List;

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
                        Unpooled.wrappedBuffer(responseObject.getContent()));
        setHeaders(responseObject, response);
        setCookies(responseObject, response);
        ctx.writeAndFlush(response);
    }

    private HttpResponseStatus getResponseStatus(final IResponse response) {
        if (null != response.getEnvironment("statusCode")) {
            return HttpResponseStatus.valueOf(response.getEnvironment("statusCode"));
        }
        return HttpResponseStatus.OK;
    }

    private void setHeaders(final IResponse responseObject, final FullHttpResponse response) {
        response.headers().set(responseObject.getEnvironment("headers"));
    }

    private void setCookies(final IResponse responseObject, final FullHttpResponse response) {
        List<Cookie> cookies = responseObject.getEnvironment("cookies");
        if (null != cookies) {
            for (Cookie cookie : cookies) {
                response.headers().set(HttpHeaders.Names.SET_COOKIE,
                        ServerCookieEncoder.encode(cookie));
            }
        }
    }
}
