package info.smart_tools.smartactors.ioc.ioc_container_simple;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc.iioccontainer.IContainer;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for IOC Container
 */
public class ContainerTest {

    @Test
    public void checkContainerCreation()
            throws InvalidArgumentException {
        IContainer container = new Container();
        assertNotNull(container);
        IKey key1 = container.getIocKey();
        IKey key2 = container.getKeyForKeyByNameResolutionStrategy();
        assertNull(key1);
        assertNotNull(key2);
    }

    @Test (expected = ResolutionException.class)
    public void checkRegisterResolveRemove()
            throws Exception {
        IContainer container = new Container();
        Object value = new Object();
        IKey strategyKey = mock(IKey.class);
        Object[] param = new Object[]{};
        IResolveDependencyStrategy strategy = mock(IResolveDependencyStrategy.class);
        when(strategy.resolve()).thenReturn(value);
        container.register(strategyKey, strategy);

        Object result = container.resolve(strategyKey);

        verify(strategy, times(1)).resolve(param);
        assertSame(result, value);

        container.remove(strategyKey);
        result = container.resolve(strategyKey);
        fail();
    }

    @Test (expected = ResolutionException.class)
    public void checkResolutionException()
            throws Exception {
        IContainer container = new Container();
        container.resolve(null, null);
        fail();
    }

    @Test (expected = RegistrationException.class)
    public void checkRegistrationException()
            throws Exception {
        IContainer container = new Container();
        container.register(null, null);
        fail();
    }

    @Test (expected = DeletionException.class)
    public void checkDeletionException()
            throws Exception {
        IContainer container = new Container();
        container.remove(null);
        fail();
    }
}
