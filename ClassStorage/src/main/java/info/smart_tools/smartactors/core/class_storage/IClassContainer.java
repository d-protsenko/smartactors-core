package info.smart_tools.smartactors.core.class_storage;

import info.smart_tools.smartactors.core.class_storage.exception.ClassStorageException;

/**
 * Interface of ClassStorage
 */
public interface IClassContainer {

    /**
     * Get {@link Class} from local key-value storage by given key
     * @param key unique class identifier
     * @return specified {@link Class}
     * @throws ClassStorageException if any errors occurred
     */
    Class getClass(final Object key) throws ClassStorageException;

    /**
     * Add new dependency to the local key-value storage by given key
     * @param key unique class identifier
     * @param clazz specified {@link Class}
     * @throws ClassStorageException if any errors occurred
     */
    void addClass(final Object key, final Class clazz) throws ClassStorageException;

    /**
     * Remove dependency from local key-value storage by given key
     * @param key unique identifier of class
     * @throws ClassStorageException if any errors occurred
     */
    void removeClass(final Object key) throws ClassStorageException;
}
