package info.smart_tools.smartactors.actors.response_sender_actor.wrapper;

import info.smart_tools.smartactors.core.ichannel_handler.IChannelHandler;
import info.smart_tools.smartactors.core.iobject.IObject;

/**
 * Wrapper for {@link info.smart_tools.smartactors.actors.response_sender_actor.ResponseSenderActor}
 */
public interface ResponseMessage {
    /**
     *
     * @return channel that should use for send response
     */
    IChannelHandler getChannelHandler();

    /**
     *
     * @return response object from message processor
     */
    IObject getResponse();

    /**
     *
     * @return all message
     */
    IObject getEnvironment();

}
