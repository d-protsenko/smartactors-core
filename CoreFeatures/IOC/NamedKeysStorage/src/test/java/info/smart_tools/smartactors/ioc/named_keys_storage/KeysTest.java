package info.smart_tools.smartactors.ioc.named_keys_storage;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.ioc.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Keys}
 */
public class KeysTest {

    @Test
    public void checkGetOrAdd()
            throws Exception {
        IStrategyContainer strategyContainer = mock(IStrategyContainer.class);
        IResolutionStrategy strategy = mock(IResolutionStrategy.class);
        IKey key = mock(IKey.class);
        ScopeProvider.subscribeOnCreationNewScope(
                scope -> {
                    try {
                        scope.setValue(IOC.getIocKey(), strategyContainer);
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }
        );
        Object scopeId = ScopeProvider.createScope(null);
        IScope scope = ScopeProvider.getScope(scopeId);
        ScopeProvider.setCurrentScope(scope);
        when(strategyContainer.resolve(any())).thenReturn(strategy);
        when(strategy.resolve("test")).thenReturn(key);
        IKey result = Keys.getOrAdd("test");
        assertNotNull(result);
        assertEquals(result, key);
    }
}

