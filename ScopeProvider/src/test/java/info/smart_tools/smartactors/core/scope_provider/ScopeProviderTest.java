package info.smart_tools.smartactors.core.scope_provider;

import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope_provider_container.IScopeProviderContainer;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests for ScopeProvider
 */
public class ScopeProviderTest {

    @Test
    public void checkGetScope()
            throws Exception {
        IScope scope = mock(IScope.class);
        IScopeProviderContainer container = mock(IScopeProviderContainer.class);
        Object key = mock(Object.class);
        Field field = ScopeProvider.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, container);
        field.setAccessible(false);
        when(container.getScope(key)).thenReturn(scope);
        IScope result = ScopeProvider.getScope(key);
        assertEquals(result, scope);
        verify(container, times(1)).getScope(key);
        reset(scope);
        reset(container);
        reset(key);
    }

    @Test
    public void checkGetCurrentScope()
            throws Exception {
        IScope scope = mock(IScope.class);
        IScopeProviderContainer container = mock(IScopeProviderContainer.class);
        Field field = ScopeProvider.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, container);
        field.setAccessible(false);
        when(container.getCurrentScope()).thenReturn(scope);
        IScope result = ScopeProvider.getCurrentScope();
        assertEquals(result, scope);
        verify(container, times(1)).getCurrentScope();
        reset(scope);
        reset(container);
    }

    @Test
    public void checkAddScope()
            throws Exception {
        final Map<Object, IScope> testMap = new HashMap<Object, IScope>();
        Object key = new Object();
        IScope scope = mock(IScope.class);
        IScopeProviderContainer container = mock(IScopeProviderContainer.class);
        Field field = ScopeProvider.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, container);
        field.setAccessible(false);
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object key = invocationOnMock.getArguments()[0];
                IScope scope = (IScope)invocationOnMock.getArguments()[1];
                testMap.put(key, scope);
                return null;
            }
        }).when(container).addScope(key, scope);
        ScopeProvider.addScope(key, scope);
        verify(container, times(1)).addScope(key, scope);
        assertEquals(testMap.get(key), scope);
        reset(scope);
        reset(container);
    }

    @Test
    public void checkSetCurrentScope()
            throws Exception {
        final Map<Object, IScope> testMap = new HashMap<Object, IScope>();
        IScope scope = mock(IScope.class);
        IScopeProviderContainer container = mock(IScopeProviderContainer.class);
        Field field = ScopeProvider.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, container);
        field.setAccessible(false);
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                testMap.put(0,(IScope)invocationOnMock.getArguments()[0]);
                return null;
            }
        }).when(container).setCurrentScope(scope);
        ScopeProvider.setCurrentScope(scope);
        verify(container, times(1)).setCurrentScope(scope);
        assertEquals(testMap.get(0), scope);
        reset(scope);
        reset(container);
    }

    @Test
    public void checkDeleteScope()
            throws Exception {
        final Map<Object, IScope> testMap = new HashMap<Object, IScope>();
        Object key = new Object();
        IScope scope = mock(IScope.class);
        IScopeProviderContainer container = mock(IScopeProviderContainer.class);
        Field field = ScopeProvider.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, container);
        field.setAccessible(false);
        testMap.put(key, scope);
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                testMap.remove(invocationOnMock.getArguments()[0]);
                return null;
            }
        }).when(container).deleteScope(key);
        ScopeProvider.deleteScope(key);
        verify(container, times(1)).deleteScope(key);
        assertEquals(testMap.size(), 0);
        reset(scope);
        reset(container);
    }

    @Test
    public void checkCreateScope()
            throws Exception {
        final Map<Object, IScope> testMap = new HashMap<Object, IScope>();
        Object args = new Object();
        IScopeProviderContainer container = mock(IScopeProviderContainer.class);
        Field field = ScopeProvider.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, container);
        field.setAccessible(false);
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                IScope scope = mock(IScope.class);
                testMap.put(0, scope);
                return null;
            }
        }).when(container).createScope(args);
        assertEquals(testMap.size(), 0);
        ScopeProvider.createScope(args);
        verify(container, times(1)).createScope(args);
        assertEquals(testMap.size(), 1);
        reset(container);
    }
}
