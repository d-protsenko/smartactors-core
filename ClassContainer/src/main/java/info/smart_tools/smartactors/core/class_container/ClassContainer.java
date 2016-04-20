package info.smart_tools.smartactors.core.class_container;

import info.smart_tools.smartactors.core.class_storage.IClassContainer;
import info.smart_tools.smartactors.core.class_storage.exception.ClassStorageException;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link IClassContainer}
 * <pre>
 * Implementation features:
 * - key-value storage of {@link Object}-{@link Class} pairs
 * </pre>
 */
public class ClassContainer implements IClassContainer {

    /**
     * Local storage of all {@link Class} instances by unique identifier
     */
    private Map<Object, Class> classStorage = new HashMap<Object, Class>();

    /**
     * Get {@link Class} from local key-value storage by given key
     * @param key unique class identifier
     * @return specified {@link Class}
     * @throws ClassStorageException if any errors occurred
     */
    public Class getClass(final Object key) throws ClassStorageException {
        return classStorage.get(key);
    }

    /**
     * Add new dependency to the local key-value storage by given key
     * @param key unique class identifier
     * @param clazz specified {@link Class}
     * @throws ClassStorageException if any errors occurred
     */
    public void addClass(final Object key, final Class clazz) throws ClassStorageException {
        classStorage.put(key, clazz);
    }

    /**
     * Remove dependency from local key-value storage by given key
     * @param key unique identifier of class
     * @throws ClassStorageException if any errors occurred
     */
    public void removeClass(final Object key) throws ClassStorageException {
        classStorage.remove(key);
    }
}
