package info.smart_tools.smartactors.web_socket_endpoint.web_socket_endpoint;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import io.netty.channel.*;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.function.Predicate;

/**
 * Channel handler that maintains a collection of open web-socket connections associated with their unique identifiers.
 */
@ChannelHandler.Sharable
public class WebSocketConnectionLifecycleMonitor extends ChannelInboundHandlerAdapter {
    private final Predicate<Object> handshakeFinishEventPred;
    private final WebSocketConnectionLifecycleListener listener;
    private final IResolveDependencyStrategy connectionIdStrategy;

    private final AttributeKey<Object> idAttributeKey = AttributeKey.valueOf("connectionId");

    private final GenericFutureListener<ChannelFuture> channelCloseListener;

    /**
     * The constructor.
     *
     * @param handshakeFinishEventPred    predicate that is {@code true} for a user event fired when web-socket handshake finishes
     * @param listener                    listener that should be notified on connection status changes
     * @param connectionIdStrategy        strategy resolving new connection identifier for a channel
     */
    public WebSocketConnectionLifecycleMonitor(final Predicate<Object> handshakeFinishEventPred,
                                               final WebSocketConnectionLifecycleListener listener,
                                               final IResolveDependencyStrategy connectionIdStrategy) {
        this.handshakeFinishEventPred = handshakeFinishEventPred;

        this.channelCloseListener = future -> {
            Channel channel = future.channel();
            Object id = channel.attr(idAttributeKey).get();
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
        Object oldId = channel.attr(idAttributeKey).setIfAbsent(id);
        id = oldId == null ? id : oldId;
        listener.onNewConnection(id, channel);
        channel.closeFuture().addListener(channelCloseListener);
    }
}
