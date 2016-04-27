package info.smart_tools.smartactors.core.scope_provider_container;

import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope.IScopeFactory;
import info.smart_tools.smartactors.core.scope_provider.IScopeProviderContainer;
import info.smart_tools.smartactors.core.scope_provider.exception.ScopeProviderException;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import static org.junit.Assert.assertEquals;

public class ScopeProviderContainerTest {

    @Test
    public void checkCreation() {
        IScopeProviderContainer scopeProviderContainer = new ScopeProviderContainer(mock(IScopeFactory.class));
        assertNotNull(scopeProviderContainer);
    }

    @Test
    public void checkScopeGetterSetterCreatorRemover()
            throws Exception {
        IScope scope = mock(IScope.class);
        IScopeFactory factory = mock(IScopeFactory.class);
        IScopeProviderContainer scopeProviderContainer = new ScopeProviderContainer(factory);
        Object param = new Object();
        when(factory.createScope(param)).thenReturn(scope);
        Object key1 = scopeProviderContainer.createScope(param);
        IScope resultScope1 = scopeProviderContainer.getScope(key1);
        assertEquals(scope, resultScope1);
        String key2 = "key2";
        scopeProviderContainer.addScope(key2, scope);
        IScope resultScope2 = scopeProviderContainer.getScope(key2);
        assertEquals(scope, resultScope2);
    }

    @Test (expected = ScopeProviderException.class)
    public void checkGettingAbsentScope()
            throws Exception {
        IScopeFactory factory = mock(IScopeFactory.class);
        IScopeProviderContainer scopeProvider = new ScopeProviderContainer(factory);
        scopeProvider.getScope("key1");
    }
//
//    @Test (expected = ScopeProviderException.class)
//    public void checkScopeDeletion() {
//        IScopeProviderContainer scopeProvider = new ScopeProviderContainer();
//        IScope scope = new Scope(null);
//        scopeProvider.setScope("scope_1", scope);
//        scopeProvider.deleteScope("scope_1");
//        scopeProvider.getScope("scope_1");
//    }
}
