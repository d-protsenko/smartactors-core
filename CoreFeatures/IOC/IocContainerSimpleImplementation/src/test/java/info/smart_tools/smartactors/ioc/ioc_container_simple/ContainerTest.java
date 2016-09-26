package info.smart_tools.smartactors.ioc.ioc_container_simple;

import info.smart_tools.smartactors.ioc.iioccontainer.IContainer;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        IKey key2 = container.getKeyForKeyStorage();
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
