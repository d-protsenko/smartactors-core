package info.smart_tools.smartactors.web_socket_endpoint.web_socket_endpoint;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.SerializeException;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.concurrent.ConcurrentMap;

/**
 * A object that sends messages to client connected using web-socket.
 */
public class WebSocketSender {
    private final ConcurrentMap<Object, Channel> connectionsMap;

    /**
     * The constructor.
     *
     * @param connectionsMap    the map from connection identifier to channel
     */
    public WebSocketSender(final ConcurrentMap<Object, Channel> connectionsMap) {
        this.connectionsMap = connectionsMap;
    }

    /**
     * Message wrapper.
     */
    public interface Wrapper {
        /**
         * @return identifier of connection
         * @throws ReadValueException if error occurs reading value
         */
        Object getConnectionId() throws ReadValueException;

        /**
         * @return message to send to client
         * @throws ReadValueException if error occurs reading value
         */
        IObject getMessage() throws ReadValueException;
    }

    /**
     * Send a message to client.
     *
     * @param wrapper    message wrapper
     * @throws ReadValueException if error occurs reading value from message
     * @throws SerializeException if error occurs serializing the message
     */
    public void send(final Wrapper wrapper)
            throws ReadValueException, SerializeException {
        Channel channel = connectionsMap.get(wrapper.getConnectionId());

        String serialized = wrapper.getMessage().serialize();
        channel.write(new TextWebSocketFrame(serialized));
    }
}
