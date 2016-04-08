package info.smart_tools.smartactors.core.iobject;

import java.util.regex.Pattern;

/**
 * A {@code FieldName} passed to {@code IObject} methods
 * as name of needed field. This class checks the validity of the name.
 */
public class FieldName {

    /**
     * Pattern with valid symbols for {@value FieldName}
     */
    private static final Pattern VALID_SYMBOLS = Pattern.compile("[\\wа-яА-ЯёЁ\\-\\+=\\|!@#\\$%\\^&\\*:/\\., \\{\\}\\(\\)\\[\\]]+");

    private String name;

    /**
     * Base constructor for {@value FieldName}
     * @param name is name of field, it must not be {@code null} and
     *             must contain at least one of the symbols from {@literal 0-9a-zA-Z_-+=|!@#$%^&*:/., {}()[]}
     * @throws IllegalArgumentException if name is not valid
     */
    public FieldName(final String name) {
        initialize(name);
    }

    @Override
    public int hashCode() {
        return 23 + name.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return (this == obj) ||
                ((obj instanceof FieldName)
                        && this.name.equals(((FieldName) obj).name)
                );
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Initialize class property {@value FieldName}
     * @param name name of field
     */
    protected void initialize(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name parameter must not be null");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name parameter must not be empty");
        }
        if (!VALID_SYMBOLS.matcher(name).matches()) {
            throw new IllegalArgumentException("Name parameter contains illegal symbols");
        }
        this.name = name;
    }
}

