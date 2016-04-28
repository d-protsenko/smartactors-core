package info.smart_tools.smartactors.core.ioc_container;

import info.smart_tools.smartactors.core.ioc.IContainer;
import info.smart_tools.smartactors.core.ioc.IKey;
import info.smart_tools.smartactors.core.ioc.exception.RegistrationException;
import info.smart_tools.smartactors.core.ioc.exception.ResolutionException;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope.IScopeFactory;
import info.smart_tools.smartactors.core.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.core.scope_provider.IScopeProviderContainer;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.scope_provider.exception.ScopeProviderException;

import info.smart_tools.smartactors.core.scope_provider_container.ScopeProviderContainer;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for IOC Container
 */
public class ContainerTest {

    /** Key for getting instance of {@link IStrategyContainer} from current scope */
    private static final String STRATEGY_CONTAINER_KEY = "strategy_container";

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

        when(scope.getValue(STRATEGY_CONTAINER_KEY)).thenReturn(strategyContainer);
        when(strategyContainer.resolve(key)).thenReturn(strategy);
        when(strategy.resolve(value)).thenReturn(value);
        Object result = container.resolve(key, value);
        verify(scope, times(1)).getValue(STRATEGY_CONTAINER_KEY);
        verify(strategyContainer, times(1)).resolve(key);
        verify(strategy,times(1)).resolve(value);
        assertEquals(result.getClass(), Long.class);
        assertEquals(value , result);
    }

    @Test (expected = ResolutionException.class)
    public void checkResolutionException()
            throws ResolutionException {
        IContainer container = new Container();
        IKey<Long> key = mock(IKey.class);
        container.resolve(key, 1L);
    }

    @Test
    public void checkRegistration()
            throws Exception {
        Long checkValue = 1L;
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
        when(scope.getValue(STRATEGY_CONTAINER_KEY)).thenReturn(strategyContainer);
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                IKey key = (IKey)invocationOnMock.getArguments()[0];
                IResolveDependencyStrategy strategy = (IResolveDependencyStrategy)invocationOnMock.getArguments()[1];
                testMap.put(key, strategy);
                return null;
            }
        }).when(strategyContainer).register(key, strategy);
        container.register(key, strategy);
        verify(scope, times(1)).getValue(STRATEGY_CONTAINER_KEY);
        verify(strategyContainer, times(1)).register(key, strategy);
        assertEquals(testMap.get(key), strategy);
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
    }
}
