package info.smart_tools.smartactors.core.iscope_provider;

import info.smart_tools.smartactors.core.iscope.IScope;

/**
 * Scope provider interface
 * Provides IScope instance by unique identifier from internal storage
 */
public interface IScopeProvider {

    /**
     * Get {@link info.smart_tools.smartactors.core.iscope.IScope} instance from internal storage
     * @param key unique identifier for instance of a object
     * @return instance of IScope
     */
    IScope getScope(final Object key);
}
