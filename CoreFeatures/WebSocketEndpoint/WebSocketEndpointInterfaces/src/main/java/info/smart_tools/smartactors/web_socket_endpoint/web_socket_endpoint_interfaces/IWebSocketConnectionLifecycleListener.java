package info.smart_tools.smartactors.web_socket_endpoint.web_socket_endpoint_interfaces;

import info.smart_tools.smartactors.web_socket_endpoint.web_socket_endpoint_interfaces.exception.ConnectionListenerException;
import io.netty.channel.Channel;

/**
 * Interface for a object being notified on web-socket server connections status changes.
 */
public interface IWebSocketConnectionLifecycleListener {
    /**
     * Called when a new connection to web-socket server is started.
     *
     * @param id         connection identifier
     * @param channel    connection channel
     * @throws ConnectionListenerException if any error occurs
     */
    void onNewConnection(Object id, Channel channel) throws ConnectionListenerException;

    /**
     * Called when a web-socket connection is closed.
     *
     * @param id         connection identifier
     * @param channel    connection channel
     * @throws ConnectionListenerException if any error occurs
     */
    void onClosedConnection(Object id, Channel channel) throws ConnectionListenerException;
}
