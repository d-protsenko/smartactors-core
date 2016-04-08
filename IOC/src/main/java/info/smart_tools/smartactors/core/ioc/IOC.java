package info.smart_tools.smartactors.core.ioc;

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
     * Resolve dependency by given given classId and args
     * @param classId unique class identifier
     * @param arg needed arguments for resolve dependency
     * @return instance of class with classId identifier
     * @throws ResolutionException when resolution is impossible because of any error
     */
    public static Object resolve(final Object classId, final Object... args)
            throws ResolutionException {
        return container.resolve(classId, args);
    }

    /**
     * Resolve instance of class {@link T} using given arguments
     * Deprecated method can be used if clazz.getClass() equals classId
     * @param clazz the class to resolve
     * @param args the arguments
     * @param <T> the class to resolve
     * @return resolved instance of class {@link T}
     * @throws ResolutionException when resolution is impossible because of any error
     */
    @Deprecated
    public static <T> T resolve(final Class<T> clazz, final Object... args)
            throws ResolutionException {
        return (T)container.resolve(clazz.getClass(), args);
    }
}
