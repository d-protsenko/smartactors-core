package info.smart_tools.smartactors.ioc.ioc_container;

import info.smart_tools.smartactors.ioc.iioccontainer.IContainer;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.ioc.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
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
        assertNotNull(key1);
        assertNotNull(key2);
        assertNotEquals(key1, key2);
        IContainer otherContainer = new Container();
        IKey otherKey1 = otherContainer.getIocKey();
        IKey otherKey2 = otherContainer.getKeyForKeyStorage();
        assertNotNull(otherKey1);
        assertNotNull(otherKey2);
        assertNotEquals(otherKey1, otherKey2);
        assertNotEquals(key1, otherKey1);
        assertNotEquals(key2, otherKey2);
    }

    @Test
    public void checkResolve()
            throws Exception {
        IContainer container = new Container();
        Object value = new Object();
        IScope scope = mock(IScope.class);
        IKey strategyKey = mock(IKey.class);
        Object[] param = new Object[]{};
        ScopeProvider.setCurrentScope(scope);
        IStrategyContainer strategyContainer = mock(IStrategyContainer.class);
        IResolveDependencyStrategy strategy = mock(IResolveDependencyStrategy.class);
        when(scope.getValue(container.getIocKey())).thenReturn(strategyContainer);
        when(strategyContainer.resolve(strategyKey)).thenReturn(strategy);
        when(strategy.resolve(param)).thenReturn(value);


        Object result = container.resolve(strategyKey);

        verify(scope, times(1)).getValue(container.getIocKey());
        verify(strategyContainer, times(1)).resolve(strategyKey);
        verify(strategy, times(1)).resolve(param);
        assertSame(value, result);

        reset(scope);
        reset(strategyContainer);
        reset(strategy);
        reset(strategyKey);
    }

    @Test (expected = ResolutionException.class)
    public void checkResolutionException()
            throws Exception {
        IContainer container = new Container();
        container.resolve(null, null);
    }

    @Test
    public void checkRegistration()
            throws Exception {
        IContainer container = new Container();
        IKey key = mock(IKey.class);
        IResolveDependencyStrategy strategy = mock(IResolveDependencyStrategy.class);
        IStrategyContainer strategyContainer = mock(IStrategyContainer.class);
        IScope scope = mock(IScope.class);
        ScopeProvider.setCurrentScope(scope);
        when(scope.getValue(container.getIocKey())).thenReturn(strategyContainer);
        doNothing().when(strategyContainer).register(key, strategy);
        container.register(key, strategy);
        verify(scope, times(1)).getValue(container.getIocKey());
        verify(strategyContainer, times(1)).register(key, strategy);
        reset(key);
        reset(strategy);
        reset(strategyContainer);
        reset(scope);
    }

    @Test (expected = RegistrationException.class)
    public void checkRegistrationException() throws Exception {
        IContainer container = new Container();
        container.register(null, null);
    }

    @Test
    public void checkGetIocGuid()
            throws InvalidArgumentException {
        IContainer container = new Container();
        assertNotNull(container.getIocKey());
        assertNotNull(container.getIocKey().toString());
        IContainer container1 = new Container();
        assertNotEquals(container.getIocKey(), container1.getIocKey());
        assertNotEquals(container.getIocKey().toString(), container1.getIocKey().toString());
    }

    @Test
    public void checkGetKeyForKeyStorage()
            throws InvalidArgumentException {
        IContainer container = new Container();
        assertNotNull(container.getKeyForKeyStorage());
        assertNotNull(container.getKeyForKeyStorage().toString());
        IContainer container1 = new Container();
        assertNotEquals(container.getKeyForKeyStorage(), container1.getKeyForKeyStorage());
        assertNotEquals(container.getKeyForKeyStorage().toString(), container1.getKeyForKeyStorage().toString());
    }

    @Test
    public void checkRemove()
            throws Exception {
        IContainer container = new Container();
        IScope scope = mock(IScope.class);
        IKey strategyKey = mock(IKey.class);
        ScopeProvider.setCurrentScope(scope);
        IStrategyContainer strategyContainer = mock(IStrategyContainer.class);
        when(scope.getValue(container.getIocKey())).thenReturn(strategyContainer);
        doNothing().when(strategyContainer).remove(strategyKey);

        container.remove(strategyKey);

        verify(scope, times(1)).getValue(container.getIocKey());
        verify(strategyContainer, times(1)).remove(strategyKey);
        reset(scope);
        reset(strategyKey);
        reset(strategyContainer);
    }

    @Test (expected = DeletionException.class)
    public void checkDeletionException() throws Exception {
        IContainer container = new Container();
        container.remove(null);
    }
}
