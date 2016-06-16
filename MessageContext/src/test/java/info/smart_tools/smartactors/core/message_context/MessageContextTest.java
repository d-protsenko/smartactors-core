package info.smart_tools.smartactors.core.message_context;

import info.smart_tools.smartactors.core.iobject.IObject;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link MessageContext}.
 */
public class MessageContextTest {
    private IMessageContextContainer messageContextContainerMock;

    @Before
    public void setUp()
            throws Exception {
        messageContextContainerMock = mock(IMessageContextContainer.class);

        Field field = MessageContext.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, messageContextContainerMock);
        field.setAccessible(false);
    }

    @Test
    public void Should_callGetterMethodOfContainer()
            throws Exception {
        IObject contextMock = mock(IObject.class);
        when(messageContextContainerMock.getCurrentContext()).thenReturn(contextMock);

        assertSame(contextMock, MessageContext.get());
    }

    @Test
    public void Should_callSetterMethodOfContainer()
            throws Exception {
        IObject contextMock = mock(IObject.class);

        MessageContext.set(contextMock);

        verify(messageContextContainerMock).setCurrentContext(contextMock);
        verifyNoMoreInteractions(messageContextContainerMock);
    }
}
