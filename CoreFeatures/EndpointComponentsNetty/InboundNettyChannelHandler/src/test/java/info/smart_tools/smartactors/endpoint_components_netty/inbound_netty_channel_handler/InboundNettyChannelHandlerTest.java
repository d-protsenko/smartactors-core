package info.smart_tools.smartactors.endpoint_components_netty.inbound_netty_channel_handler;

import info.smart_tools.smartactors.base.interfaces.iaction.IFunction0;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IDefaultMessageContext;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandlerCallback;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Test for {@link InboundNettyChannelHandler}.
 */
public class InboundNettyChannelHandlerTest {
    private class Message {}

    @Test public void Should_createContextAndSendItToCallback() throws Exception {
        IFunction0<IDefaultMessageContext<Message, Void, Channel>> ctxProvider = mock(IFunction0.class);
        IDefaultMessageContext<Message, Void, Channel> ctxMock = mock(IDefaultMessageContext.class);
        IMessageHandlerCallback<IDefaultMessageContext<Message, Void, Channel>> callback = mock(IMessageHandlerCallback.class);
        ChannelHandlerContext handlerContext = mock(ChannelHandlerContext.class);
        Channel channel = mock(Channel.class);

        when(ctxProvider.execute()).thenReturn(ctxMock).thenThrow(AssertionError.class);
        when(handlerContext.channel()).thenReturn(channel);

        InboundNettyChannelHandler handler = new InboundNettyChannelHandler<Message>(callback, ctxProvider, Message.class);

        Message message = new Message();

        doAnswer(invocationOnMock -> {
            verify(ctxMock).setConnectionContext(same(channel));
            verify(ctxMock).setSrcMessage(same(message));

            return null;
        }).when(callback).handle(same(ctxMock));

        handler.channelRead(handlerContext, message);

        verify(callback).handle(same(ctxMock));
    }
}
