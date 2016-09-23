package info.smart_tools.smartactors.core.message_bus;

import info.smart_tools.smartactors.core.iioccontainer.IContainer;
import info.smart_tools.smartactors.core.imessage_bus_container.IMessageBusContainer;
import info.smart_tools.smartactors.core.imessage_bus_container.exception.SendingMessageException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.message_bus_container_with_scope.MessageBusContainer;
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
}
