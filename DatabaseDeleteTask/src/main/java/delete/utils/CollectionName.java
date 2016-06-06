package delete.utils;

import java.util.regex.Pattern;

public class CollectionName {
    protected static Pattern VALIDATION_PATTERN = Pattern.compile("[a-zA-Z_][0-9a-zA-Z_]*");

    String name;

    private CollectionName(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public static CollectionName fromString(String name)
        throws IllegalArgumentException {
        if(!VALIDATION_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid collection name: " + name);
        }

        return new CollectionName(name.toLowerCase());
    }
}
