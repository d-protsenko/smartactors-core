package info.smart_tools.smartactors.web_socket_endpoint.web_socket_sender;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.web_socket_endpoint.web_socket_sender.exception.UnknownConnectionIdException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link WebSocketSender}.
 */
public class WebSocketSenderTest {
    private IObject messageMock;
    private WebSocketSender.Wrapper wrapperMock;
    private ConcurrentMap<Object, Channel> channelMap;
    private Channel channelMock;

    @Before
    public void setUp()
            throws Exception {
        messageMock = mock(IObject.class);

        when(messageMock.serialize()).thenReturn("{\"mock\":true}");

        wrapperMock = mock(WebSocketSender.Wrapper.class);

        when(wrapperMock.getMessage()).thenReturn(messageMock);
        when(wrapperMock.getConnectionId()).thenReturn("id-0-0");

        channelMock = mock(Channel.class);

        channelMap = new ConcurrentHashMap<>();
        channelMap.put("id-0-0", channelMock);
    }

    @Test
    public void Should_sendTextFrames()
            throws Exception {
        WebSocketSender webSocketSender = new WebSocketSender(channelMap);

        webSocketSender.send(wrapperMock);

        ArgumentCaptor<TextWebSocketFrame> frameArgumentCaptor = ArgumentCaptor.forClass(TextWebSocketFrame.class);

        verify(channelMock).writeAndFlush(frameArgumentCaptor.capture());

        assertEquals("{\"mock\":true}", frameArgumentCaptor.getValue().text());
    }

    @Test(expected = UnknownConnectionIdException.class)
    public void Should_throwWnenConnectionIdIsNotExist()
            throws Exception {
        WebSocketSender webSocketSender = new WebSocketSender(channelMap);

        when(wrapperMock.getConnectionId()).thenReturn("id-z-0");
        webSocketSender.send(wrapperMock);
    }

    @Test
    public void Should_closeConnection()
            throws Exception {
        ChannelFuture writeFutureMock = mock(ChannelFuture.class);
        when(channelMock.writeAndFlush(any())).thenReturn(writeFutureMock);

        WebSocketSender webSocketSender = new WebSocketSender(channelMap);

        webSocketSender.disconnect(wrapperMock);

        ArgumentCaptor<WebSocketFrame> frameArgumentCaptor = ArgumentCaptor.forClass(WebSocketFrame.class);
        verify(channelMock).writeAndFlush(frameArgumentCaptor.capture());
        verify(writeFutureMock).addListener(same(ChannelFutureListener.CLOSE));
    }
}
