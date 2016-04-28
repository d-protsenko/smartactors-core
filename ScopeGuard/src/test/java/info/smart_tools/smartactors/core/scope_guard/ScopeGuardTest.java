package info.smart_tools.smartactors.core.scope_guard;

import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.scope_guard.exception.ScopeGuardException;
import info.smart_tools.smartactors.core.scope_provider.IScopeProviderContainer;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for ScopeGuard
 */
public class ScopeGuardTest {

    @Test
    public void checkGuard()
            throws Exception {
        IScope scope1 = mock(IScope.class);
        IScope scope2 = mock(IScope.class);
        IScopeProviderContainer container = mock(IScopeProviderContainer.class);
        Field field = ScopeProvider.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, container);
        field.setAccessible(false);

        when(container.getCurrentScope()).thenReturn(scope1).thenReturn(scope2).thenReturn(scope1);
        doNothing().when(container).setCurrentScope(scope1);
        doNothing().when(container).setCurrentScope(scope2);
        ScopeProvider.setCurrentScope(scope1);
        Object key = new Object();
        when(container.getScope(key)).thenReturn(scope2);

        ScopeGuard guard = new ScopeGuard();
        guard.guard(key);
        assertEquals(scope2, ScopeProvider.getCurrentScope());
        guard.close();
        assertEquals(scope1, ScopeProvider.getCurrentScope());
        verify(container, times(3)).getCurrentScope();
        verify(container, times(2)).setCurrentScope(scope1);
        verify(container, times(1)).setCurrentScope(scope2);
        reset(container);
    }

    @Test (expected = ScopeGuardException.class)
    public void checkScopeGuardExceptionOnGuard()
            throws Exception {
        ScopeGuard guard = new ScopeGuard();
        Object key = new Object();
        guard.guard(key);
        fail();
    }

    @Test (expected = ScopeGuardException.class)
    public void checkScopeGuardExceptionOnClose() throws ScopeGuardException {
        ScopeGuard guard = new ScopeGuard();
        guard.close();
        fail();
    }
}
