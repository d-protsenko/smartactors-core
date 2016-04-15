package info.smart_tools.smartactors.core.scope_guard_java6;

import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.scope_guard_java6.exception.ScopeGuardException;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;

/**
 * Implementation of {@link IScopeGuard}
 */
public class ScopeGuard implements IScopeGuard {

    /**
     * Local storage for instance of {@link IScope}
     */
    private IScope previousScope;

    /**
     * Locally save and substitute current instance of {@link info.smart_tools.smartactors.core.iscope.IScope} by
     * other
     * @param key unique identifier for find {@link info.smart_tools.smartactors.core.iscope.IScope}
     * @throws  ScopeGuardException if any errors occurred
     */
    public void guard(final Object key)
            throws ScopeGuardException {
        try {
            previousScope = ScopeProvider.getCurrentScope();
            ScopeProvider.setCurrentScope(ScopeProvider.getScope(key));
        } catch (Exception e) {
            throw new ScopeGuardException("ScopeGuard could not to switch scope.", e);
        }
    }

    /**
     * Set locally saved {@link info.smart_tools.smartactors.core.iscope.IScope} as current
     * @throws  ScopeGuardException if any errors occurred
     */
    public void close()
            throws ScopeGuardException {
        try {
            if (previousScope != null) {
                ScopeProvider.setCurrentScope(previousScope);
            }
        } catch (Exception e) {
            throw new ScopeGuardException("ScopeGuard could not restore original state.", e);
        }
    }
}
