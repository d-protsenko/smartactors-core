package info.smart_tools.smartactors.core.message_bus;

import info.smart_tools.smartactors.core.imessage_bus_container.IMessageBusContainer;
import info.smart_tools.smartactors.core.imessage_bus_container.exception.SendingMessageException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link MessageBus}.
 */
public class MessageBusTest {

    IMessageBusContainer container = mock(IMessageBusContainer.class);

    @Before
    public void init() throws Exception {
        Field field = MessageBus.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, this.container);
        field.setAccessible(false);
    }

    @Test
    public void checkGettingMessageBusKey() {
        MessageBus.getMessageBusKey();
        verify(this.container, times(1)).getMessageBusKey();
    }

    @Test
    public void checkSendingMessage()
            throws SendingMessageException {
        IObject message = mock(IObject.class);
        MessageBus.send(message);
        verify(this.container, times(1)).send(message);
    }

    @Test (expected = SendingMessageException.class)
    public void checkExceptionOnSendingMessage()
            throws SendingMessageException {
        doThrow(new SendingMessageException("")).when(this.container).send(null);
        MessageBus.send(null);
        fail();
    }

    @Test
    public void checkSendingMessageWithSpecificChain()
            throws SendingMessageException {
        IObject message = mock(IObject.class);
        Object chainName = mock(Object.class);
        MessageBus.send(message, chainName);
        verify(this.container, times(1)).send(message, chainName);
    }

    @Test (expected = SendingMessageException.class)
    public void checkExceptionOnSendingMessageWithSpecificChain()
            throws SendingMessageException {
        doThrow(new SendingMessageException("")).when(this.container).send(null, null);
        MessageBus.send(null, null);
        fail();
    }

    @Test
    public void checkSendingMessageWithReply()
            throws Exception {
        IObject message = mock(IObject.class);
        Object chainNameForReply = mock(Object.class);
        MessageBus.sendAndReply(message, chainNameForReply);
        verify(this.container, times(1)).sendAndReply(message, chainNameForReply);
    }

    @Test (expected = SendingMessageException.class)
    public void checkExceptionOnSendingMessageWithReply()
            throws SendingMessageException {
        doThrow(new SendingMessageException("")).when(this.container).sendAndReply(null, null);
        MessageBus.sendAndReply(null, null);
        fail();
    }

    @Test
    public void checkSendingMessageWithSpecificChainAndReply()
            throws Exception {
        IObject message = mock(IObject.class);
        Object chainName = mock(Object.class);
        Object chainNameForReply = mock(Object.class);
        MessageBus.sendAndReply(message, chainName, chainNameForReply);
        verify(this.container, times(1)).sendAndReply(message, chainName, chainNameForReply);
    }

    @Test (expected = SendingMessageException.class)
    public void checkExceptionOnSendingMessageWithSpecificChainAndReply()
            throws SendingMessageException {
        doThrow(new SendingMessageException("")).when(this.container).sendAndReply(null, null, null);
        MessageBus.sendAndReply(null, null, null);
        fail();
    }
}
