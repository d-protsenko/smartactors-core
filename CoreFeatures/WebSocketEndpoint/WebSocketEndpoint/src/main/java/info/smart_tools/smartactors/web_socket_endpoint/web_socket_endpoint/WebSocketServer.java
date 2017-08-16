package info.smart_tools.smartactors.web_socket_endpoint.web_socket_endpoint;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.http_endpoint.tcp_server.TcpServer;
import info.smart_tools.smartactors.web_socket_endpoint.connection_lifecycle_monitor.WebSocketConnectionLifecycleMonitor;
import info.smart_tools.smartactors.web_socket_endpoint.web_socket_endpoint_interfaces.IWebSocketConnectionLifecycleListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

import java.util.UUID;

/**
 *
 */
public class WebSocketServer extends TcpServer {
    private final String path;
    private final int maxContentLength;
    private final ChannelHandler connectionLifecycleMonitor;

    /**
     * Constructor.
     *
     * @param port               TCP port
     * @param requestHandler     request handler
     * @param path               web-socket path
     * @param maxContentLength   max. content length
     * @param listener           the listener that should be notified on new and closed connections
     */
    public WebSocketServer(
            final int port,
            final ChannelHandler requestHandler,
            final String path,
            final int maxContentLength,
            final IWebSocketConnectionLifecycleListener listener) {
        super(port, requestHandler);
        this.path = path;
        this.maxContentLength = maxContentLength;

        this.connectionLifecycleMonitor = new WebSocketConnectionLifecycleMonitor(
                x -> x == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE,
                listener, new IResolveDependencyStrategy() {
            @Override
            public <T> T resolve(final Object... args) throws ResolveDependencyStrategyException {
                return (T) UUID.randomUUID().toString();
            }
        });
    }

    @Override
    protected ChannelPipeline setupPipeline(final ChannelPipeline pipeline) {
        return super.setupPipeline(pipeline)
                .addLast(
                        new HttpServerCodec(),
                        new HttpObjectAggregator(maxContentLength),
                        new WebSocketServerProtocolHandler(path),
                        connectionLifecycleMonitor
                );
    }
}
