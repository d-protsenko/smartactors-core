package info.smart_tools.smartactors.core.scope_provider_implementation;

import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope.IScopeFactory;
import info.smart_tools.smartactors.core.iscope_provider.IScopeProvider;
import info.smart_tools.smartactors.core.iscope_provider.exception.ScopeProviderException;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link info.smart_tools.smartactors.core.iscope_provider.IScopeProvider}
 */
public class ScopeProvider implements IScopeProvider {

    /**
     * Local storage of all {@link info.smart_tools.smartactors.core.iscope.IScope} instances by unique identifier
     */
    private Map<Object, IScope> scopeStorage = new HashMap<Object, IScope>();

    /**
     * Default constructor
     */
    private ScopeProvider() {
    }

    /**
     * Instance of {@link IScopeFactory}
     */
    private IScopeFactory factory;

    /**
     * Constructor
     * @param factory instance of {@link IScopeFactory}
     */
    public ScopeProvider(final IScopeFactory factory) {
        this.factory = factory;
    }

    /**
     * Realization of {@link info.smart_tools.smartactors.core.iscope_provider.IScopeProvider} getScope method
     * Get {@link info.smart_tools.smartactors.core.iscope.IScope} instance from local storage.
     * @param key unique identifier for instance of an object
     * @return instance of {@link info.smart_tools.smartactors.core.iscope.IScope}
     * @throws ScopeProviderException if value is not found or any error occurred
     */
    public IScope getScope(final Object key) throws ScopeProviderException {
        IScope scope = scopeStorage.get(key);
        if (scope == null) {
            throw new ScopeProviderException("Scope not found.");
        }

        return scope;
    }

    /**
     * Realization of {@link info.smart_tools.smartactors.core.iscope_provider.IScopeProvider} setScope method
     * Put {@link info.smart_tools.smartactors.core.iscope.IScope} instance to the local storage
     * @param key unique identifier for instance of an object
     * @param scope given instance of {@link info.smart_tools.smartactors.core.iscope.IScope}
     * @throws ScopeProviderException if any errors occurred
     */
    public void setScope(final Object key, final IScope scope)
            throws ScopeProviderException {
        try {
            scopeStorage.put(key, scope);
        } catch (Exception e) {
            throw new ScopeProviderException("Error was occurred", e);
        }
    }

    /**
     * Realization of {@link info.smart_tools.smartactors.core.iscope_provider.IScopeProvider} deleteScope method
     * Remove {@link info.smart_tools.smartactors.core.iscope.IScope} instance from local storage by given key
     * @param key unique identifier for instance of an object
     * @throws ScopeProviderException if any errors occurred
     */
    public void deleteScope(final Object key)
            throws ScopeProviderException {
        try {
            scopeStorage.remove(key);
        } catch (Exception e) {
            throw new ScopeProviderException("Error was occurred", e);
        }
    }

    /**
     * Create new instance of {@link IScopeFactory}
     * @param params needed parameters for creation
     * @return new instance of {@link IScope}
     * @throws ScopeProviderException if any errors occurred
     */
    public IScope createScope(final Object params) throws ScopeProviderException {
        try {
            return factory.createScope(params);
        } catch (Exception e) {
            throw new ScopeProviderException("Failed to create instance of IScope.", e);
        }
    }
}
