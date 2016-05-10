package info.smart_tools.smartactors.core.ioc_container;

import info.smart_tools.smartactors.core.iioccontainer.IContainer;
import info.smart_tools.smartactors.core.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope.IScopeFactory;
import info.smart_tools.smartactors.core.iscope_provider_container.IScopeProviderContainer;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;

import info.smart_tools.smartactors.core.scope_provider_container.ScopeProviderContainer;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doAnswer;
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
    public void checkContainerCreation() {
        IContainer container = new Container();
        assertNotNull(container);
    }

    @Test
    public void checkResolve()
            throws Exception {
        Long value = 1L;
        IContainer container = new Container();
        IScopeProviderContainer scopeContainer = new ScopeProviderContainer(mock(IScopeFactory.class));

        Field field = ScopeProvider.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, scopeContainer);
        field.setAccessible(false);

        IScope scope = mock(IScope.class);
        IStrategyContainer strategyContainer = mock(IStrategyContainer.class);
        IResolveDependencyStrategy strategy = mock(IResolveDependencyStrategy.class);
        IKey<Long> key = mock(IKey.class);

        ScopeProvider.setCurrentScope(scope);

        when(scope.getValue(container.getIocKey())).thenReturn(strategyContainer);
        when(strategyContainer.resolve(key)).thenReturn(strategy);
        when(strategy.resolve(value)).thenReturn(value);
        Object result = container.resolve(key, value);
        verify(scope, times(1)).getValue(container.getIocKey());
        verify(strategyContainer, times(1)).resolve(key);
        verify(strategy,times(1)).resolve(value);
        assertEquals(result.getClass(), Long.class);
        assertEquals(value , result);
        reset(scope);
        reset(strategyContainer);
        reset(strategy);
        reset(key);
    }

    @Test (expected = ResolutionException.class)
    public void checkResolutionException()
            throws ResolutionException {
        IContainer container = new Container();
        IKey<Long> key = mock(IKey.class);
        container.resolve(key, 1L);
        reset(key);
    }

    @Test
    public void checkRegistration()
            throws Exception {
        IContainer container = new Container();
        IScopeProviderContainer scopeContainer = new ScopeProviderContainer(mock(IScopeFactory.class));
        final Map<IKey, IResolveDependencyStrategy> testMap = new HashMap<IKey, IResolveDependencyStrategy>();

        Field field = ScopeProvider.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, scopeContainer);
        field.setAccessible(false);

        IKey<Long> key = mock(IKey.class);
        IScope scope = mock(IScope.class);
        IStrategyContainer strategyContainer = mock(IStrategyContainer.class);
        IResolveDependencyStrategy strategy = mock(IResolveDependencyStrategy.class);
        ScopeProvider.setCurrentScope(scope);
        when(scope.getValue(container.getIocKey())).thenReturn(strategyContainer);
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                IKey key = (IKey)invocationOnMock.getArguments()[0];
                IResolveDependencyStrategy strategy = (IResolveDependencyStrategy)invocationOnMock.getArguments()[1];
                testMap.put(key, strategy);
                return null;
            }
        }).when(strategyContainer).register(key, strategy);
        container.register(key, strategy);
        verify(scope, times(1)).getValue(container.getIocKey());
        verify(strategyContainer, times(1)).register(key, strategy);
        assertEquals(testMap.get(key), strategy);
        reset(scope);
        reset(strategyContainer);
        reset(strategy);
        reset(key);
    }

    @Test (expected = RegistrationException.class)
    public void checkRegistrationException() throws Exception {
        IContainer container = new Container();
        IScopeProviderContainer scopeContainer = mock(IScopeProviderContainer.class);

        Field field = ScopeProvider.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, scopeContainer);
        field.setAccessible(false);

        IKey<Long> key = mock(IKey.class);

        doThrow(new ScopeProviderException("ScopeProviderException")).when(scopeContainer).getCurrentScope();
        container.register(key, null);
        reset(key);
    }

    @Test
    public void checkGetIocGuid() {
        IContainer container = new Container();
        assertNotNull(container.getIocKey());
        assertNotNull(container.getIocKey().toString());
        IContainer container1 = new Container();
        assertNotEquals(container.getIocKey(), container1.getIocKey());
        assertNotEquals(container.getIocKey().toString(), container1.getIocKey().toString());
    }

    @Test
    public void checkGetKeyForKeyStorage() {
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
        IScopeProviderContainer scopeContainer = new ScopeProviderContainer(mock(IScopeFactory.class));
        final Map<IKey, IResolveDependencyStrategy> testMap = new HashMap<IKey, IResolveDependencyStrategy>();

        Field field = ScopeProvider.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, scopeContainer);
        field.setAccessible(false);

        IKey<Long> key = mock(IKey.class);
        IScope scope = mock(IScope.class);
        IStrategyContainer strategyContainer = mock(IStrategyContainer.class);
        IResolveDependencyStrategy strategy = mock(IResolveDependencyStrategy.class);
        ScopeProvider.setCurrentScope(scope);
        testMap.put(key, strategy);
        when(scope.getValue(container.getIocKey())).thenReturn(strategyContainer);
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                IKey key = (IKey)invocationOnMock.getArguments()[0];
                testMap.remove(key);
                return null;
            }
        }).when(strategyContainer).remove(key);
        container.remove(key);
        verify(scope, times(1)).getValue(container.getIocKey());
        verify(strategyContainer, times(1)).remove(key);
        assertEquals(testMap.size(), 0);
        reset(scope);
        reset(strategyContainer);
        reset(strategy);
        reset(key);
    }

    @Test (expected = DeletionException.class)
    public void checkDeletionException() throws Exception {
        IContainer container = new Container();
        IScopeProviderContainer scopeContainer = mock(IScopeProviderContainer.class);

        Field field = ScopeProvider.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, scopeContainer);
        field.setAccessible(false);

        IKey<Long> key = mock(IKey.class);

        doThrow(new ScopeProviderException("ScopeProviderException")).when(scopeContainer).getCurrentScope();
        container.remove(key);
        reset(key);
    }
}
