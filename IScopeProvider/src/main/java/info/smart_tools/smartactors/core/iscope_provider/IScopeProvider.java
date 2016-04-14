package info.smart_tools.smartactors.core.iscope_provider;

import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope_provider.exception.ScopeProviderException;

/**
 * Scope provider interface
 * Provides IScope instance by unique identifier from internal storage
 */
public interface IScopeProvider {

    /**
     * Get {@link info.smart_tools.smartactors.core.iscope.IScope} instance from internal storage
     * @param key unique identifier for instance of a object
     * @throws ScopeProviderException if value is not found or any error occurred
     * @return instance of IScope
     */
    IScope getScope(final Object key)
            throws ScopeProviderException;

    /**
     * Stores an value in scope associating it with defined key
     * @param key given key
     * @param value given value
     * @throws ScopeProviderException if any error occurred
     */
    void setScope(final Object key, final IScope value)
            throws ScopeProviderException;

    /**
     * Removes the value associated with key
     * @param key given key
     * @throws ScopeProviderException if value is absent or any errors occurred
     */
    void deleteScope(final Object key)
            throws ScopeProviderException;

    /**
     * Create new instance of {@link IScope}
     * @param params needed parameters for creation
     * @return new instance of IScope
     * @throws ScopeProviderException if any errors occurred
     */
    IScope createScope(final Object params)
            throws ScopeProviderException;
}
