package info.smart_tools.smartactors.core.ioc_container;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IContainer;
import info.smart_tools.smartactors.core.ioc.IKey;
import info.smart_tools.smartactors.core.ioc.exception.RegistrationException;
import info.smart_tools.smartactors.core.ioc.exception.ResolutionException;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope.IScopeFactory;
import info.smart_tools.smartactors.core.iscope.exception.ScopeException;
import info.smart_tools.smartactors.core.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.core.istrategy_container.exception.StrategyContainerException;
import info.smart_tools.smartactors.core.scope_provider.IScopeProviderContainer;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.scope_provider.exception.ScopeProviderException;

import info.smart_tools.smartactors.core.scope_provider_container.ScopeProviderContainer;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class ContainerTest {

    /** Key for getting instance of {@link IStrategyContainer} from current scope */
    private static final String STRATEGY_CONTAINER_KEY = "strategy_container";
    /** Key for getting class_id from {@link IObject} */
    private static final String CLASS_ID_KEY = "class_id";
    /** Key for getting args from {@link IObject} */
    private static final String ARGS_KEY = "args";
    /** Key for getting strategy_id from {@link IObject} */
    private static final String STRATEGY_ID_KEY = "strategy_id";
    /** Key for getting strategy_args from {@link IObject} */
    private static final String STRATEGY_ARGS_KEY = "strategy_args";



    @Test
    public void checkContainerCreation() {
        IContainer container = new Container();
        assertNotNull(container);
    }

    @Test
    public void checkResolve()
            throws Exception {
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
        when(strategy.resolve(1L)).thenReturn(1L);
        Object result = container.resolve(key, 1L);
        assertEquals(result.getClass(), Long.class);
        assertEquals(1L , result);
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
        Mockito.doNothing().when(strategyContainer).register(key, strategy);

        when(strategyContainer.resolve(key)).thenReturn(strategy);
        when(strategy.resolve(1L)).thenReturn(checkValue);

        container.register(key, strategy);
        Object result = container.resolve(key, 1L);
        assertEquals(result.getClass(), checkValue.getClass());
        assertEquals(result, checkValue);
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
