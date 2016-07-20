package info.smart_tools.smartactors.core.iresponse_sender;

import info.smart_tools.smartactors.core.ichannel_handler.IChannelHandler;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iresponse.IResponse;
import io.netty.channel.ChannelHandlerContext;

public interface IResponseSender {
    void send(final IResponse responseObject, final IObject environment, final IChannelHandler ctx)
            throws ResolutionException;
}
