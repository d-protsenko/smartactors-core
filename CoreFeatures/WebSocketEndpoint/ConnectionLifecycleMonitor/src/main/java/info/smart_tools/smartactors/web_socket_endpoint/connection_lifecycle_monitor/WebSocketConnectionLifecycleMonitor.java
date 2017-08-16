package info.smart_tools.smartactors.web_socket_endpoint.connection_lifecycle_monitor;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.web_socket_endpoint.web_socket_endpoint_interfaces.IWebSocketConnectionLifecycleListener;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.function.Predicate;

import static info.smart_tools.smartactors.web_socket_endpoint.connection_lifecycle_monitor.ChannelAttributes.CONNECTION_ID_ATTRIBUTE;

/**
 * Channel handler that maintains a collection of open web-socket connections associated with their unique identifiers.
 */
@ChannelHandler.Sharable
public class WebSocketConnectionLifecycleMonitor extends ChannelInboundHandlerAdapter {
    private final Predicate<Object> handshakeFinishEventPred;
    private final IWebSocketConnectionLifecycleListener listener;
    private final IResolveDependencyStrategy connectionIdStrategy;

    private final GenericFutureListener<ChannelFuture> channelCloseListener;

    /**
     * The constructor.
     *
     * @param handshakeFinishEventPred    predicate that is {@code true} for a user event fired when web-socket handshake finishes
     * @param listener                    listener that should be notified on connection status changes
     * @param connectionIdStrategy        strategy resolving new connection identifier for a channel
     */
    public WebSocketConnectionLifecycleMonitor(final Predicate<Object> handshakeFinishEventPred,
                                               final IWebSocketConnectionLifecycleListener listener,
                                               final IResolveDependencyStrategy connectionIdStrategy) {
        this.handshakeFinishEventPred = handshakeFinishEventPred;

        this.channelCloseListener = future -> {
            Channel channel = future.channel();
            Object id = channel.attr(CONNECTION_ID_ATTRIBUTE).get();
            listener.onClosedConnection(id, channel);
        };
        this.listener = listener;
        this.connectionIdStrategy = connectionIdStrategy;
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt)
            throws Exception {
        if (handshakeFinishEventPred.test(evt)) {
            onHandshakeFinished(ctx);
        }

        super.userEventTriggered(ctx, evt);
    }

    private void onHandshakeFinished(final ChannelHandlerContext ctx)
            throws Exception {
        Channel channel = ctx.channel();
        Object id = connectionIdStrategy.resolve(channel);
        Object oldId = channel.attr(CONNECTION_ID_ATTRIBUTE).setIfAbsent(id);
        id = oldId == null ? id : oldId;
        listener.onNewConnection(id, channel);
        channel.closeFuture().addListener(channelCloseListener);
    }
}
