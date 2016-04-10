package info.smart_tools.smartactors.core.scope_provider_implementation;

import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope_provider.IScopeProvider;
import info.smart_tools.smartactors.core.iscope_provider.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.recursive_scope.Scope;
import org.junit.Test;
import static org.junit.Assert.*;

public class ScopeProviderTest {

    @Test
    public void checkCreation() {
        IScopeProvider scopeProvider = new ScopeProvider();
        assertNotNull(scopeProvider);
    }

    @Test
    public void checkStoringAndGettingScope() {
        IScopeProvider scopeProvider = new ScopeProvider();
        IScope scope = new Scope(null);
        scopeProvider.setScope("scope_1", scope);
        IScope stored_scope = scopeProvider.getScope("scope_1");
        assertEquals(scope, stored_scope);
    }

    @Test (expected = ScopeProviderException.class)
    public void checkGettingAbsentScope() {
        IScopeProvider scopeProvider = new ScopeProvider();
        scopeProvider.getScope("scope_1");
    }

    @Test (expected = ScopeProviderException.class)
    public void checkScopeDeletion() {
        IScopeProvider scopeProvider = new ScopeProvider();
        IScope scope = new Scope(null);
        scopeProvider.setScope("scope_1", scope);
        scopeProvider.deleteScope("scope_1");
        scopeProvider.getScope("scope_1");
    }
}
