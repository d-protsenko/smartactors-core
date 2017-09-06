package info.smart_tools.smartactors.endpoint_components_netty.inbound_netty_channel_handler;

import info.smart_tools.smartactors.base.interfaces.iaction.IFunction0;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IDefaultMessageContext;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandlerCallback;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Netty channel handler that sends received messages to message handler pipeline.
 *
 * @param <TNettyMsg>
 */
public class InboundNettyChannelHandler<TNettyMsg> extends SimpleChannelInboundHandler<TNettyMsg> {
    private final IMessageHandlerCallback<IDefaultMessageContext<TNettyMsg, Void, Channel>> pipelineCallback;
    private final IFunction0<IDefaultMessageContext<TNettyMsg, Void, Channel>> contextFactory;

    /**
     * The constructor.
     *
     * @param pipelineCallback callback to call when message received
     * @param contextFactory   function creating message contexts
     * @param imClass          type of inbound Netty message
     */
    public InboundNettyChannelHandler(
            final IMessageHandlerCallback<IDefaultMessageContext<TNettyMsg, Void, Channel>> pipelineCallback,
            final IFunction0<IDefaultMessageContext<TNettyMsg, Void, Channel>> contextFactory,
            final Class<TNettyMsg> imClass) {
        super(imClass);
        this.pipelineCallback = pipelineCallback;
        this.contextFactory = contextFactory;
    }

    @Override
    protected void channelRead0(
            final ChannelHandlerContext ctx,
            final TNettyMsg msg) throws Exception {
        IDefaultMessageContext<TNettyMsg, Void, Channel> context = contextFactory.execute();

        context.setSrcMessage(msg);
        context.setConnectionContext(ctx.channel());
        context.setDstMessage(null);

        pipelineCallback.handle(context);
    }
}
