package info.smart_tools.smartactors.core.class_storage;

import info.smart_tools.smartactors.core.class_storage.exception.ClassStorageException;

/**
 * Realization of class storage by ServiceLocator pattern
 */
public final class ClassStorage {

    /**
     * Private default constructor
     */
    private ClassStorage() {
    }

    /**
     * Implementation of {@link IClassStorageContainer}.
     * Must be initialized before ClassStorage will be used.
     * Initialization possible only with using java reflection API
     * Example:
     * <pre>
     * {@code
     * Field field = ClassStorage.class.getDeclaredField("container");
     * field.setAccessible(true);
     * field.set(null, new Object());
     * field.setAccessible(false);
     * }
     * </pre>
     */
    private static IClassStorageContainer container;

    /**
     * Get {@link Class} from local key-value storage by given key
     * @param key unique class identifier
     * @return specified {@link Class}
     * @throws ClassStorageException if any errors occurred
     */
    public static Class<?> getClass(final Object key)
            throws ClassStorageException {
        return container.getClass(key);
    }

    /**
     * Add new dependency to the local key-value storage by given key
     * @param key unique class identifier
     * @param clazz specified {@link Class}
     * @throws ClassStorageException if any errors occurred
     */
    public static void addClass(final Object key, final Class<?> clazz)
            throws ClassStorageException {
        container.addClass(key, clazz);
    }

    /**
     * Remove dependency from local key-value storage by given key
     * @param key unique identifier of class
     * @throws ClassStorageException if any errors occurred
     */
    public static void removeClass(final Object key)
            throws ClassStorageException {
        container.removeClass(key);
    }
}
