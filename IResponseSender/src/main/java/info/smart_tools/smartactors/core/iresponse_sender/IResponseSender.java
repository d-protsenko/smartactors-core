package info.smart_tools.smartactors.core.iresponse_sender;

import info.smart_tools.smartactors.core.iresponse.IResponse;
import io.netty.channel.ChannelHandlerContext;

public interface IResponseSender {
    void send(final IResponse responseObject, final ChannelHandlerContext ctx);
}
