package info.smart_tools.smartactors.endpoint_components_netty.ssl_channel_initialization_handler;

import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IDefaultMessageContext;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandlerCallback;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.exception.MessageHandlerException;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.helpers.IBypassMessageHandler;
import io.netty.channel.Channel;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

/**
 * Handler that adds SSL handler to Netty pipeline.
 *
 * @param <T>
 */
public class SslChannelInitializationHandler<T extends IDefaultMessageContext<Channel, ?, ?>>
        implements IBypassMessageHandler<T> {
    private final SSLEngine engine;

    /**
     * The constructor.
     *
     * @param engine SSL engine to use
     */
    public SslChannelInitializationHandler(SSLEngine engine) {
        this.engine = engine;
    }

    @Override
    public void handle(
            final IMessageHandlerCallback<T> next,
            final T context) throws MessageHandlerException {
        context.getSrcMessage().pipeline()
                .addFirst("ssl", new SslHandler(engine));
        next.handle(context);
    }
}
