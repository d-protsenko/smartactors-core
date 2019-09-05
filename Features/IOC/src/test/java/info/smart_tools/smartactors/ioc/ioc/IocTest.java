package info.smart_tools.smartactors.ioc.ioc;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.istrategy.IStrategy;
import info.smart_tools.smartactors.ioc.iioccontainer.IContainer;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.ioc.string_ioc_key.Key;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.iscope.exception.ScopeException;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
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

    private IStrategyContainer sc = mock(IStrategyContainer.class);
    IStrategy strategy = mock(IStrategy.class);

    @Before
    public void changeIocContainer()
            throws Exception {
//        ScopeProvider.clearListOfSubscribers();
        ScopeProvider.subscribeOnCreationNewScope(
                scope -> {
                    try {
                        scope.setValue(IOC.getIocKey(), sc);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        Object scopeKey = ScopeProvider.createScope(null);
        IScope scope = ScopeProvider.getScope(scopeKey);

        ScopeProvider.setCurrentScope(scope);
        IOC.register(IOC.getKeyForKeyByNameStrategy(), new ResolveByNameIocStrategy(
                (a) -> {
                    try {
                        return new Key((String) a[0]);
                    } catch (InvalidArgumentException e) {
                        throw new RuntimeException(e);
                    }
                })
        );
    }

    @Test
    public void checkResolution()
            throws Exception {
        Object value = new Object();
        IKey key = mock(IKey.class);
        Object param = new Object();

        when(this.sc.resolve(key)).thenReturn(this.strategy);
        when(this.strategy.resolve(param)).thenReturn(value);
        Object result = IOC.resolve(key, param);
        assertSame(result, value);
        verify(this.sc, times(1)).resolve(key);
        verify(this.strategy, times(1)).resolve(param);
    }

    @Test
    public void checkRegistration()
            throws Exception {
        IKey key = mock(IKey.class);
        IStrategy strategy = mock(IStrategy.class);
        doNothing().when(this.sc).register(key, strategy);
        IOC.register(key, strategy);
        verify(this.sc, times(1)).register(key, strategy);
    }

    @Test
    public void checkDeletion()
            throws Exception {
        IKey key = mock(IKey.class);
        when(this.sc.unregister(key)).thenReturn(null);
        IOC.unregister(key);
        verify(this.sc, times(1)).unregister(key);
    }

    @Test
    public void checkGetIocGuid()
            throws Exception {
        IKey key = IOC.getIocKey();
        assertNotNull(key);
    }

    @Test
    public void checkgetKeyForKeyByNameStrategy()
            throws Exception {
        IKey key = IOC.getKeyForKeyByNameStrategy();
        assertNotNull(key);
    }
}
