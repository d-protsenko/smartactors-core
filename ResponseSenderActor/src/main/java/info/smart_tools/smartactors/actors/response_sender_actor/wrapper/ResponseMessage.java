package info.smart_tools.smartactors.actors.response_sender_actor.wrapper;

import info.smart_tools.smartactors.core.ichannel_handler.IChannelHandler;
import info.smart_tools.smartactors.core.iobject.IObject;

public interface ResponseMessage {
    IChannelHandler getChannelHandler();

    IObject getResponse();

    IObject getEnvironment();

}
