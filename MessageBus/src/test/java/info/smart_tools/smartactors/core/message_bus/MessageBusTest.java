package info.smart_tools.smartactors.core.message_bus;

import info.smart_tools.smartactors.core.iioccontainer.IContainer;
import info.smart_tools.smartactors.core.imessage_bus_container.IMessageBusContainer;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.message_bus_container_with_scope.MessageBusContainer;
import org.junit.Before;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;

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

    @
}
