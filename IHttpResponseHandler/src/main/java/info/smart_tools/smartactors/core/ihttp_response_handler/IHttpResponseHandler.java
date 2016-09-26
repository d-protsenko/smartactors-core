package info.smart_tools.smartactors.core.ihttp_response_handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * Interface for response handlers
 */
public interface IHttpResponseHandler {
    void handle(ChannelHandlerContext ctx, FullHttpResponse response);
}
