package info.smart_tools.smartactors.web_socket_endpoint.web_socket_endpoint;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

/**
 * Channel handler that maintains a collection of open web-socket connections associated with their unique identifiers.
 */
@ChannelHandler.Sharable
public class WebSocketConnectionLifecycleHandler extends ChannelInboundHandlerAdapter {
    private final Predicate<Object> handshakeFinishEventPred;
    private final ConcurrentMap<Object, Channel> channelsMap;
    private final IResolveDependencyStrategy connectionIdStrategy;

    private final AttributeKey<Object> idAttributeKey = AttributeKey.valueOf("connectionId");

    private final GenericFutureListener<ChannelFuture> channelCloseListener;

    /**
     * The constructor.
     *
     * @param handshakeFinishEventPred    predicate that is {@code true} for a user event fired when web-socket handshake finishes
     * @param channelsMap                 map from connection identifier to a client channel
     * @param connectionIdStrategy        strategy resolving new connection identifier for a channel
     */
    public WebSocketConnectionLifecycleHandler(final Predicate<Object> handshakeFinishEventPred,
                                               final ConcurrentMap<Object, Channel> channelsMap,
                                               final IResolveDependencyStrategy connectionIdStrategy) {
        this.handshakeFinishEventPred = handshakeFinishEventPred;
        this.channelsMap = channelsMap;

        this.channelCloseListener = future -> {
            Channel channel = future.channel();
            Object id = channel.attr(idAttributeKey).get();
            channelsMap.remove(id, channel);
        };
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
        Object id = channel.attr(idAttributeKey).setIfAbsent(connectionIdStrategy.resolve(channel));
        channelsMap.put(id, channel);
        channel.closeFuture().addListener(channelCloseListener);
    }
}
