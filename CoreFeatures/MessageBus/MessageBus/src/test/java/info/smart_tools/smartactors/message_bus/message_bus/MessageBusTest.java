package info.smart_tools.smartactors.message_bus.message_bus;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.message_bus.interfaces.imessage_bus_container.IMessageBusContainer;
import info.smart_tools.smartactors.message_bus.interfaces.imessage_bus_container.exception.SendingMessageException;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

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
        MessageBus.send(message, true);
        verify(this.container, times(2)).send(message, true);
    }

    @Test (expected = SendingMessageException.class)
    public void checkExceptionOnSendingMessage()
            throws SendingMessageException {
        doThrow(new SendingMessageException("")).when(this.container).send(null, true);
        MessageBus.send(null);
        fail();
    }

    @Test
    public void checkSendingMessageWithSpecificChain()
            throws SendingMessageException {
        IObject message = mock(IObject.class);
        Object chainName = mock(Object.class);
        MessageBus.send(message, chainName);
        MessageBus.send(message, chainName, true);
        verify(this.container, times(2)).send(message, chainName, true);
    }

    @Test (expected = SendingMessageException.class)
    public void checkExceptionOnSendingMessageWithSpecificChain()
            throws SendingMessageException {
        doThrow(new SendingMessageException("")).when(this.container).send(null, null, true);
        MessageBus.send(null, null);
        fail();
    }

    @Test
    public void checkSendingMessageWithReply()
            throws Exception {
        IObject message = mock(IObject.class);
        Object chainNameForReply = mock(Object.class);
        MessageBus.sendAndReply(message, chainNameForReply);
        MessageBus.sendAndReply(message, chainNameForReply, true);
        verify(this.container, times(2)).sendAndReply(message, chainNameForReply, true);
    }

    @Test (expected = SendingMessageException.class)
    public void checkExceptionOnSendingMessageWithReply()
            throws SendingMessageException {
        doThrow(new SendingMessageException("")).when(this.container).sendAndReply(null, null, true);
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
        MessageBus.sendAndReply(message, chainName, chainNameForReply, true);
        verify(this.container, times(2)).sendAndReply(message, chainName, chainNameForReply, true);
    }

    @Test (expected = SendingMessageException.class)
    public void checkExceptionOnSendingMessageWithSpecificChainAndReply()
            throws SendingMessageException {
        doThrow(new SendingMessageException("")).when(this.container).sendAndReply(null, null, null, true);
        MessageBus.sendAndReply(null, null, null);
        fail();
    }
}
