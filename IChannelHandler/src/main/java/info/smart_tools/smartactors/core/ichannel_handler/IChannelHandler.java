package info.smart_tools.smartactors.core.ichannel_handler;

/**
 * Interface for channel handler, that should send response to client
 */
public interface IChannelHandler<T> {
    /**
     * Method for initialize channel handler
     * @param channelHandler
     */
    void init(T channelHandler);

    /**
     * Method for sending response
     *
     * @param response Object of the response
     */
    void send(final Object response);
}
