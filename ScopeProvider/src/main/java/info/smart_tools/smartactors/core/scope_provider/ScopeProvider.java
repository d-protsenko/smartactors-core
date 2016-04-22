package info.smart_tools.smartactors.core.scope_provider;

import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.scope_provider.exception.ScopeProviderException;

/**
 * Realization of ScopeProvider by ServiceLocator pattern
 */
public final class ScopeProvider {

    /**
     * Implementation of {@link IScopeProviderContainer}.
     * Must be initialized before ScopeProvider will be used.
     * Initialization possible only with using java reflection API
     * Example:
     * <pre>
     * {@code
     * Field field = ScopeProvider.class.getDeclaredField("container");
     * field.setAccessible(true);
     * field.set(null, new Object());
     * field.setAccessible(false);
     * }
     * </pre>
     */
    private static IScopeProviderContainer container;

    /**
     * Private default constructor
     */
    private ScopeProvider() {
    }

    /**
     * Get instance of {@link IScope} from local storage of {@link IScopeProviderContainer} by unique
     * identifier of {@link IScope} instance
     * @param key unique identifier of {@link IScope} instance
     * @return instance of {@link IScope}
     * @throws ScopeProviderException if value is not found or any errors occurred
     */
    public static IScope getScope(final Object key)
            throws ScopeProviderException {
        return container.getScope(key);
    }

    /**
     * Get current instance of {@link IScope}
     * @return instance of {@link IScope}
     * @throws ScopeProviderException if current scope is null or any errors occurred
     */
    public static IScope getCurrentScope()
            throws ScopeProviderException {
        return container.getCurrentScope();
    }

    /**
     * Add new dependency to the local storage of {@link IScopeProviderContainer} by unique identifier
     * @param key unique identifier
     * @param scope instance of {@link IScope}
     * @throws ScopeProviderException if any errors occurred
     */
    public static void addScope(final Object key, final IScope scope)
            throws ScopeProviderException {
        container.addScope(key, scope);
    }

    /**
     * Set given instance of {@link IScope} as current to the {@link IScopeProviderContainer}
     * @param scope instance of {@link IScope}
     * @throws ScopeProviderException if any errors occurred
     */
    public static void setCurrentScope(final IScope scope)
            throws ScopeProviderException {
        container.setCurrentScope(scope);
    }

    /**
     * Remove given instance of {@link IScope} from local storage of {@link IScopeProviderContainer} by unique identifier
     * @param key unique identifier {@link IScope} instance
     * @throws ScopeProviderException if any errors occurred
     */
    public static void deleteScope(final Object key)
            throws ScopeProviderException {
        container.deleteScope(key);
    }

    /**
     * Create new instance of {@link IScope} and store it to the local storage of {@link IScopeProviderContainer}
     * @param param needed parameters for creation instance of {@link IScope}
     * @return unique identifier of created instance of {@link IScope}
     * @throws ScopeProviderException if any errors occurred
     */
    public static Object createScope(final Object param)
            throws ScopeProviderException {
        return container.createScope(param);
    }
}
