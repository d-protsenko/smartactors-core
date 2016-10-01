package info.smart_tools.smartactors.core.http_client_handler;

import info.smart_tools.smartactors.core.iresponse_handler.IResponseHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * Handler for responses, that received after sending request
 */
public class HttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    private final IResponseHandler httpResponseHandler;

    public HttpClientHandler(final IResponseHandler handler) {
        this.httpResponseHandler = handler;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, final FullHttpResponse response) throws Exception {
        httpResponseHandler.handle(channelHandlerContext, response);
    }
}
