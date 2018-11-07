package info.smart_tools.smartactors.ioc.named_field_names_storage;

import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;

/**
 * Service locator for storing named instances of {@link IKey}
 */
public final class Keys {

    /**
     * Default private constructor
     */
    private Keys() {
    }

    /**
     * Resolve instance of {@link IKey} by given name
     * @param keyName name of instance of {@link IKey}
     * @throws ResolutionException if dependency resolution has failed
     * @return instance of {@link IKey}
     */
    public static IKey resolveByName(final String keyName)
            throws ResolutionException {
        return (IKey) IOC.resolve(IOC.getKeyForKeyByNameResolutionStrategy(), keyName);
    }

    /**
     * Remove dependencies of {@link IKey}s by given names
     * @param keyNames array of names of instances of {@link IKey}
     * @throws DeletionException if dependency deletion or resolution has failed
     */
    public static void removeByNames(final String[] keyNames)
            throws DeletionException {
        DeletionException exception = new DeletionException("Deletion of key(s) has failed.");

        for(String keyName : keyNames) {
            try {
                IOC.remove(Keys.resolveByName(keyName));
            } catch (ResolutionException e) {
                exception.addSuppressed(new ResolutionException(keyName));
            } catch(DeletionException e) {
                exception.addSuppressed(new DeletionException(keyName));
            }
        }
        if (exception.getSuppressed().length > 0) {
            throw exception;
        }
    }

    /**
     * Resolve instance of {@link IKey} by given name
     * @param keyName name of instance of {@link IKey}
     * @throws ResolutionException if dependency resolution has failed
     * @return instance of {@link IKey}
     */
    @Deprecated
    public static IKey getOrAdd(final String keyName)
            throws ResolutionException {
        return (IKey) IOC.resolve(IOC.getKeyForKeyByNameResolutionStrategy(), keyName);
    }
}
