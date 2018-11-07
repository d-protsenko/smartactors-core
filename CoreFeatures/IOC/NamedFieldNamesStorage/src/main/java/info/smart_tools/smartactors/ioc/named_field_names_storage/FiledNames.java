package info.smart_tools.smartactors.ioc.named_field_names_storage;

import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;

/**
 * Service locator for resolving named instances of {@link IFieldName}
 */
public final class FiledNames {

    static IKey fieldNameKey = null;

    public static void init() {
        try {
            fieldNameKey = Keys.getOrAdd(IFieldName.class.getCanonicalName());
        } catch(ResolutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Default private constructor
     */
    private FiledNames() {
    }

    /**
     * Resolve instance of {@link IFieldName} by given name
     * @param fieldName name of instance of {@link IFieldName}
     * @throws ResolutionException if dependency resolution has been failed
     * @return instance of {@link IFieldName}
     */
    public static IFieldName resolveByName(final String fieldName)
            throws ResolutionException {
        return (IFieldName) IOC.resolve(fieldNameKey, fieldName);
    }
}
