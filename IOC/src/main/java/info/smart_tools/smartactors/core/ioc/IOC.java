package info.smart_tools.smartactors.core.ioc;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.exception.RegistrationException;
import info.smart_tools.smartactors.core.ioc.exception.ResolutionException;

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
     * Resolve dependency by given given IObject instance
     * @param obj instance of IObject that contains needed parameters for resolve dependency
     * @param <T> type of class for resolution
     * @return instance of class with classId identifier
     * @throws ResolutionException if resolution is impossible because of any errors
     */
    public static <T> T resolve(final IObject obj)
            throws ResolutionException {
        return container.resolve(obj);
    }

    /**
     * Resolve instance of class {@link T} using given arguments
     * Deprecated method can be used if clazz.getClass() equals classId
     * @param clazz the class to resolve
     * @param args the arguments
     * @param <T> the class to resolve
     * @return resolved instance of class {@link T}
     * @throws RegistrationException when resolution is impossible because of any error
     */
    @Deprecated
    public static <T> T resolve(final Class<T> clazz, final Object... args)
            throws RegistrationException {
        //TODO: need to add realization after all needed core components will be implemented
        return null;
    }

    /**
     * Register new dependency
     * @param obj instance of IObject that contains needed parameters for resolve dependency
     * @throws RegistrationException when registration is impossible because of any error
     */
    void register(final IObject obj)
            throws RegistrationException {
        container.register(obj);
    }
}
