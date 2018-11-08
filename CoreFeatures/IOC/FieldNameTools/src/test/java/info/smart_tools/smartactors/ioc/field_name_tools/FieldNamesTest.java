package info.smart_tools.smartactors.ioc.field_name_tools;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.field_name_tools.FiledNames;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link FiledNames}
 */
public class FieldNamesTest {

    @Test
    public void checkResolveByName()
            throws Exception {
        IStrategyContainer strategyContainer = mock(IStrategyContainer.class);
        IResolveDependencyStrategy strategy = mock(IResolveDependencyStrategy.class);
        IKey key = mock(IKey.class);
        IKey iFieldNameKey = mock(IKey.class);
        IFieldName fieldName = mock(IFieldName.class);
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
        when(strategy.resolve(IFieldName.class.getCanonicalName())).thenReturn(iFieldNameKey);
        when(strategy.resolve("test")).thenReturn(fieldName);
        IFieldName result = FiledNames.resolveByName("test");
        assertNotNull(result);
        assertEquals(result, fieldName);
    }
}

