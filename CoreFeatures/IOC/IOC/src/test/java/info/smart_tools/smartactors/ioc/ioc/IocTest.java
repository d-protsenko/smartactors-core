package info.smart_tools.smartactors.ioc.ioc;

import info.smart_tools.smartactors.base.interfaces.istrategy.IStrategy;
import info.smart_tools.smartactors.ioc.iioccontainer.IContainer;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

/**
 * Tests for IOC
 */
public class IocTest {

    private IContainer container = mock(IContainer.class);
    private IContainer oldContainer;

    @Before
    public void changeIocContainer()
            throws Exception {
        Field field = IOC.class.getDeclaredField("container");
        field.setAccessible(true);
        oldContainer = (IContainer) field.get(null);
        field.set(null, container);
        field.setAccessible(false);
    }

    @Test
    public void checkResolution()
            throws Exception {
        Object value = new Object();
        IKey key = mock(IKey.class);
        Object param = new Object();
        when(container.resolve(key, param)).thenReturn(value);
        Object result = IOC.resolve(key, param);
        assertSame(result, value);
        verify(container, times(1)).resolve(key, param);
    }

    @Test
    public void checkRegistration()
            throws Exception {
        IKey key = mock(IKey.class);
        IStrategy strategy = mock(IStrategy.class);
        doNothing().when(container).register(key, strategy);
        IOC.register(key, strategy);
        verify(container, times(1)).register(key, strategy);
    }

    @Test
    public void checkDeletion()
            throws Exception {
        IKey key = mock(IKey.class);
        doNothing().when(container).unregister(key);
        IOC.unregister(key);
        verify(container, times(1)).unregister(key);
    }

    @Test
    public void checkGetIocGuid()
            throws Exception {
        when(container.getIocKey()).thenReturn(mock(IKey.class));
        IKey key = IOC.getIocKey();
        assertNotNull(key);
    }

    @Test
    public void checkgetKeyForKeyByNameStrategy()
            throws Exception {
        when(container.getKeyForKeyByNameStrategy()).thenReturn(mock(IKey.class));
        IKey key = IOC.getKeyForKeyByNameStrategy();
        assertNotNull(key);
    }

    @After
    public void restoreIocContainer()
            throws Exception {
        Field field = IOC.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, oldContainer);
        field.setAccessible(false);
    }
}
