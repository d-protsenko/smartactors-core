package info.smart_tools.smartactors.core.http_client_handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * Created by sevenbits on 26.09.16.
 */
public class HttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse>{

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse response) throws Exception {
        System.out.println(response);
    }
}
