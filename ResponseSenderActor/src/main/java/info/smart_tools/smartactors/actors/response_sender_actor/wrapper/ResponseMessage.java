package info.smart_tools.smartactors.actors.response_sender_actor.wrapper;

import info.smart_tools.smartactors.core.ichannel_handler.IChannelHandler;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

public interface ResponseMessage {

    HttpRequest getRequest() throws ReadValueException;

    IChannelHandler getChannelHandler();

    IObject getResponse();

    String getProtocol();

    IObject getEnvironment();

}
