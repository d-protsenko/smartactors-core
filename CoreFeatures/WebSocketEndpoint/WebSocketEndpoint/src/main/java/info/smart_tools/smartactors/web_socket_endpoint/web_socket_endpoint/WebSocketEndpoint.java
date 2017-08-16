package info.smart_tools.smartactors.web_socket_endpoint.web_socket_endpoint;

import info.smart_tools.smartactors.endpoint.endpoint_channel_inbound_handler.EndpointChannelInboundHandler;
import info.smart_tools.smartactors.endpoint.interfaces.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.web_socket_endpoint.web_socket_endpoint_interfaces.IWebSocketConnectionLifecycleListener;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 *
 */
public class WebSocketEndpoint extends WebSocketServer {
    /**
     * Constructor.
     *
     * @param port             TCP port
     * @param path             web-socket path
     * @param maxContentLength max. content length
     * @param listener         the listener that should be notified on new and closed connections
     * @param environmentHandler    the environment handler
     * @param scope
     * @param name
     * @param receiverChain
     */
    public WebSocketEndpoint(
            final int port,
            final String path,
            final int maxContentLength,
            final IWebSocketConnectionLifecycleListener listener,
            final IEnvironmentHandler environmentHandler,
            final IScope scope,
            final String name,
            final IReceiverChain receiverChain
            ) throws ResolutionException {
        super(port,
                new EndpointChannelInboundHandler<WebSocketFrame>(new WSEndpointHandler(
                    receiverChain, environmentHandler, scope, name
                ), WebSocketFrame.class),
                path, maxContentLength, listener);
    }
}
