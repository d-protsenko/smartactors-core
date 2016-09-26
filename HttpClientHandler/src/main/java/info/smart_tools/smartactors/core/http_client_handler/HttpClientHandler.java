package info.smart_tools.smartactors.core.http_client_handler;

import info.smart_tools.smartactors.core.ihttp_response_handler.IHttpResponseHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * Handler for responses, that received after sending request
 */
public class HttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    IHttpResponseHandler httpResponseHandler;

    public HttpClientHandler(final IHttpResponseHandler handler) {
        this.httpResponseHandler = handler;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, final FullHttpResponse response) throws Exception {
        httpResponseHandler.handle(channelHandlerContext, response);
    }
}
