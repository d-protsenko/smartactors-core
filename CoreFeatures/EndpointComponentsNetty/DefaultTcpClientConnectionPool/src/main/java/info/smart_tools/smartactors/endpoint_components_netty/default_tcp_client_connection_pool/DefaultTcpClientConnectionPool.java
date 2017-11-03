package info.smart_tools.smartactors.endpoint_components_netty.default_tcp_client_connection_pool;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.endpoint_components_netty.isocket_connection_pool.ISocketConnectionPool;
import info.smart_tools.smartactors.endpoint_components_netty.isocket_connection_pool.exception.SocketConnectionPoolException;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.SocketChannel;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Implementation of {@link ISocketConnectionPool} for TCP sockets.
 */
public class DefaultTcpClientConnectionPool implements ISocketConnectionPool<SocketChannel, InetSocketAddress> {
    private final ChannelFactory<? extends SocketChannel> channelFactory;
    private final Map<InetSocketAddress, ChannelCollection> channelCollections;
    private final IAction<SocketChannel> channelSetupCallback;

    /**
     * The constructor.
     *
     * @param channelFactory       channel factory that creates channels
     * @param channelSetupCallback action that configures channels (sets up the pipeline)
     */
    public DefaultTcpClientConnectionPool(
            ChannelFactory<? extends SocketChannel> channelFactory,
            IAction<SocketChannel> channelSetupCallback) {
        this.channelFactory = channelFactory;
        this.channelSetupCallback = channelSetupCallback;
        channelCollections = new ConcurrentHashMap<>();
    }

    /**
     * {@link ChannelFutureListener} that removes a channel from pool when channel gets closed.
     */
    private class ChannelCloseListener implements ChannelFutureListener {
        @Override
        public void operationComplete(ChannelFuture future)
                throws Exception {
            SocketChannel channel = (SocketChannel) future.channel();

            ChannelCollection collection = channelCollections.get(channel.remoteAddress());

            collection.closed(channel);
        }
    }

    /**
     * Collection of channels connected to the same address.
     */
    private class ChannelCollection {
        private boolean terminated = false;
        private Queue<SocketChannel> channels = new ConcurrentLinkedQueue<>();

        void recycle(SocketChannel channel) {
            if (terminated) {
                channel.close();
                processTerminated();
            } else {
                channels.add(channel);
            }
        }

        void closed(SocketChannel channel) {
            channels.remove(channel);

            if (channels.isEmpty()) {
                channelCollections.remove(channel.remoteAddress(), this);
                terminated = true;
                processTerminated();
            }
        }

        SocketChannel get() {
            SocketChannel channel;

            while (null != (channel = channels.poll())) {
                if (channel.isOpen()) {
                    return channel;
                }
            }

            return null;
        }

        void processTerminated() {
            SocketChannel channel;

            while (null != (channel = channels.poll())) {
                channel.close();
            }
        }
    }

    private final ChannelFutureListener closeFutureListener = new ChannelCloseListener();

    @Override
    public SocketChannel getChannel(InetSocketAddress address)
            throws SocketConnectionPoolException {
        SocketChannel channel = tryGetPooled(address);

        if (null != channel) {
            return channel;
        }

        return createChannel(address);
    }

    @Override
    public void recycleChannel(SocketChannel channel)
            throws SocketConnectionPoolException {
        if (channel.isOpen()) {
            ChannelCollection collection = channelCollections.computeIfAbsent(
                    channel.remoteAddress(),
                    addr -> new ChannelCollection());
            collection.recycle(channel);
        }
    }

    private SocketChannel tryGetPooled(InetSocketAddress address) {
        ChannelCollection collection = channelCollections.get(address);

        if (null == collection) {
            return null;
        }

        return collection.get();
    }

    private SocketChannel createChannel(InetSocketAddress address)
            throws SocketConnectionPoolException {
        SocketChannel channel = channelFactory.newChannel();

        channel.closeFuture().addListener(closeFutureListener);

        try {
            channelSetupCallback.execute(channel);
        } catch (ActionExecuteException | InvalidArgumentException e) {
            throw new SocketConnectionPoolException("Error occurred configuring client channel.", e);
        }

        channel.connect(address);

        return channel;
    }
}
