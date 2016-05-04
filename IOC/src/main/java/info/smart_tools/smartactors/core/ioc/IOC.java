package info.smart_tools.smartactors.core.ioc;

import info.smart_tools.smartactors.core.ioc.exception.DeletionException;
import info.smart_tools.smartactors.core.ioc.exception.RegistrationException;
import info.smart_tools.smartactors.core.ioc.exception.ResolutionException;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;

/**
 * Realization of IOC Container by ServiceLocator pattern
 */
public final class IOC {

    /**
     * Private default constructor
     */
    private IOC() {
    }

    /**
     * Implementation of {@link IContainer}.
     * Must be initialized before IOC will be used.
     * Initialization possible only with using java reflection API
     * Example:
     * <pre>
     * {@code
     * Field field = IOC.class.getDeclaredField("container");
     * field.setAccessible(true);
     * field.set(null, new Object());
     * field.setAccessible(false);
     * }
     * </pre>
     */
    private static IContainer container;

    /**
     * Return specific container ID
     * @return specific container ID
     */
    public static IKey getIocKey() {
        return container.getIocKey();
    }

    /**
     * Resolve dependency by given given {@link IKey} instance and args
     * @param key instance of {@link IKey}
     * @param args needed parameters for resolve dependency
     * @param <T> type of class for resolution
     * @return instance of class with classId identifier
     * @throws ResolutionException if resolution is impossible because of any errors
     */
    public static <T> T resolve(final IKey<T> key, final Object ... args)
            throws ResolutionException {
        return container.resolve(key, args);
    }

    /**
     * Register new dependency by instance of {@link IKey}
     * @param key instance of {@link IKey}
     * @param strategy instance of {@link IResolveDependencyStrategy}
     * @throws RegistrationException when registration is impossible because of any error
     */
    public static void register(final IKey key, final IResolveDependencyStrategy strategy)
            throws RegistrationException {
        container.register(key, strategy);
    }

    public static void remove(final IKey key)
            throws DeletionException {
        container.remove(key);
    }

}
