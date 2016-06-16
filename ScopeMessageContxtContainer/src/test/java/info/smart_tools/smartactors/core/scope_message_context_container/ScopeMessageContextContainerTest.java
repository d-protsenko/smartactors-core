package info.smart_tools.smartactors.core.scope_message_context_container;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope.exception.ScopeException;
import info.smart_tools.smartactors.core.iscope_provider_container.IScopeProviderContainer;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.message_context.IMessageContextContainer;
import info.smart_tools.smartactors.core.message_context.exceptions.MessageContextAccessException;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link ScopeMessageContextContainer}.
 */
public class ScopeMessageContextContainerTest {
    private IScopeProviderContainer scopeProviderContainerMock;
    private IScope scopeMock;

    @Before
    public void setUp()
            throws Exception {
        scopeProviderContainerMock = mock(IScopeProviderContainer.class);
        scopeMock = mock(IScope.class);

        Field field = ScopeProvider.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, scopeProviderContainerMock);
        field.setAccessible(false);
    }

    @Test
    public void Should_storeAndLoadContextUsingTheSameKey()
            throws Exception {
        ArgumentCaptor<Object> objectArgumentCaptor = ArgumentCaptor.forClass(Object.class);
        IObject contextMock = mock(IObject.class);

        when(scopeProviderContainerMock.getCurrentScope()).thenReturn(scopeMock);

        IMessageContextContainer messageContextContainer = new ScopeMessageContextContainer();

        messageContextContainer.setCurrentContext(contextMock);

        verify(scopeMock).setValue(objectArgumentCaptor.capture(), same(contextMock));

        contextMock = mock(IObject.class);
        when(scopeMock.getValue(eq(objectArgumentCaptor.getValue()))).thenReturn(contextMock);

        assertSame(contextMock, messageContextContainer.getCurrentContext());
    }

    @Test(expected = MessageContextAccessException.class)
    public void Should_get_throw_When_ScopeProviderThrows()
            throws Exception {
        when(scopeProviderContainerMock.getCurrentScope()).thenThrow(mock(ScopeProviderException.class));

        new ScopeMessageContextContainer().getCurrentContext();
    }

    @Test(expected = MessageContextAccessException.class)
    public void Should_set_throw_When_ScopeProviderThrows()
            throws Exception {
        when(scopeProviderContainerMock.getCurrentScope()).thenThrow(mock(ScopeProviderException.class));

        new ScopeMessageContextContainer().setCurrentContext(mock(IObject.class));
    }

    @Test(expected = MessageContextAccessException.class)
    public void Should_get_throw_When_ScopeThrows()
            throws Exception {
        when(scopeProviderContainerMock.getCurrentScope()).thenReturn(scopeMock);
        when(scopeMock.getValue(any())).thenThrow(mock(ScopeException.class));

        new ScopeMessageContextContainer().getCurrentContext();
    }

    @Test(expected = MessageContextAccessException.class)
    public void Should_set_throw_When_ScopeThrows()
            throws Exception {
        when(scopeProviderContainerMock.getCurrentScope()).thenReturn(scopeMock);
        doThrow(mock(ScopeException.class)).when(scopeMock).setValue(any(), any());

        new ScopeMessageContextContainer().setCurrentContext(mock(IObject.class));
    }

    @Test(expected = MessageContextAccessException.class)
    public void Should_get_throw_When_ScopeReturnsUnexpectedObject()
            throws Exception {
        when(scopeProviderContainerMock.getCurrentScope()).thenReturn(scopeMock);
        when(scopeMock.getValue(any())).thenReturn(new Object());

        new ScopeMessageContextContainer().getCurrentContext();
    }
}