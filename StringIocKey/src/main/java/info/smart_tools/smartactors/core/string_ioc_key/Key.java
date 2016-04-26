package info.smart_tools.smartactors.core.string_ioc_key;

import info.smart_tools.smartactors.core.ioc.IKey;

/**
 * Implementation of {@link info.smart_tools.smartactors.core.ioc.IKey}
 *
 * <pre>
 * This implementation has follow specific features:
 * - string unique identifier;
 * - two constructors (<T>, String) and (String);
 * - overridden equals and hashCode method based on string property;
 * </pre>
 * @param <T> class type
 */
public class Key<T> implements IKey<T> {

    private String identifier;

    /**
     * Constructor with string unique identifier
     * @param identifier string unique identifier
     */
    public Key(final String identifier) {
        this.identifier = identifier;
    }

    /**
     * Constructor with string unique identifier and class type
     * @param clazz class type
     * @param identifier string unique identifier
     */
    public Key(final Class<T> clazz, final String identifier) {
        this(identifier);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Key<?> key = (Key<?>) o;

        return identifier.equals(key.identifier);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }
}
