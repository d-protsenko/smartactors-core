package info.smart_tools.smartactors.endpoint_components_netty.default_tcp_client_connection_pool;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.endpoint_components_netty.isocket_connection_pool.ISocketConnectionPool;
import info.smart_tools.smartactors.endpoint_components_netty.isocket_connection_pool.exception.SocketConnectionPoolException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;

import java.net.InetSocketAddress;

/**
 * Connection pool that doesn't actually store connections but creates new ones when required and closes old ones when
 * they are no longer required.
 */
public class FakeTcpClientConnectionPool implements ISocketConnectionPool<SocketChannel, InetSocketAddress> {
    private final Bootstrap bootstrap;

    /**
     * The constructor.
     *
     * @param channelFactory       chanel factory
     * @param channelSetupCallback callback that configures a new channel after creation
     * @param eventLoopGroup       event loop group to be used by a channel
     */
    public FakeTcpClientConnectionPool(
            final ChannelFactory<? extends SocketChannel> channelFactory,
            final IAction<SocketChannel> channelSetupCallback,
            final EventLoopGroup eventLoopGroup) {
        bootstrap = new Bootstrap()
            .channelFactory(channelFactory)
            .group(eventLoopGroup)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(final SocketChannel ch)
                        throws Exception {
                    channelSetupCallback.execute(ch);
                }
            });
    }

    @Override
    public SocketChannel getChannel(final InetSocketAddress address)
            throws SocketConnectionPoolException {
        try {
            return (SocketChannel) bootstrap.connect(address).await().channel();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SocketConnectionPoolException(e);
        } catch (Exception e) {
            throw new SocketConnectionPoolException(e);
        }
    }

    @Override
    public void recycleChannel(final SocketChannel channel)
            throws SocketConnectionPoolException {
        channel.close();
    }
}