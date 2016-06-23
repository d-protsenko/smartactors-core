package info.smart_tools.smartactors.core.db_task.create_collection.psql;

import com.sun.istack.internal.NotNull;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * The builder for query of creation of indexes to some collection.
 */
class IndexBuilder {
    private String index;
    private String collection;
    private String field;

    /** A indexes creation templates. */
    private Map<String, List<String[]>> indexCreationTemplates = new HashMap<String, List<String[]>>() {{
        put("ordered", Collections.singletonList(new String[] { "CREATE INDEX ON ", " USING BTREE ((", "));\n" }));
        put("tags", Collections.singletonList(new String[] { "CREATE INDEX ON ", " USING GIN ((", "));\n" }));
        put("fulltext", Collections.singletonList(new String[] { "CREATE INDEX ON ",
                " USING GIN ((to_tsvector('russian',(", ")::text)));\n" }));
        put("datetime", Collections.singletonList(new String[] { "CREATE INDEX ON ",
                " USING BTREE ((parse_timestamp_immutable(", ")));\n" }));
        put("id", Arrays.asList(new String[] { "CREATE INDEX ON ", " USING BTREE ((", "));\n" },
                new String[] {"CREATE INDEX ON ", " USING HASH ((", "));\n" }));
    }};

    /** A length of indexes templates. */
    private Map<String, Integer> indexTemplatesLength = new HashMap<String, Integer>() {{
        put("ordered", 35);
        put("tags", 33);
        put("fulltext", 64);
        put("datetime", 62);
        put("id", 69);
    }};

    /**
     * Default constructor for {@link IndexBuilder}.
     *              Creates a new instance of {@link IndexBuilder}.
     */
    private IndexBuilder() {}

    /**
     * Factory method for creation a new instance of {@link IndexBuilder}.
     *
     * @return a new instance of {@link IndexBuilder}.
     */
    static IndexBuilder create() {
        return new IndexBuilder();
    }

    IndexBuilder withCollection(@Nonnull String collection) {
        this.collection = collection;
        return this;
    }

    /**
     * Appends index type for which you want to create.
     *
     * @param index - index type.
     *
     * @return a link to yourself {@link IndexBuilder}.
     */
    IndexBuilder withIndex(@NotNull String index) {
        if (!indexCreationTemplates.containsKey(index))
            throw new IllegalArgumentException("Index type - " + index + " not supported.");

        this.index = index;
        return this;
    }

    /**
     * Appends field for which you want create a index.
     *
     * @param field - field for which you want create a index
     *
     * @return a link to yourself {@link IndexBuilder}.
     */
    IndexBuilder withField(@NotNull String field) {
        this.field = field;
        return this;
    }

    /**
     * Builds query for creation of some index to some collection.
     *
     * @return a query for creation index to some collection.
     *
     * @throws BuildingException when one of the index or field are null or empty.
     */
    String build() throws BuildingException {
        requiresNonnull(index, "The index should not be a null or empty, should try invoke 'withIndex'.");
        requiresNonnull(collection, "The collection should not be a null or empty, should try invoke 'withCollection'.");
        requiresNonnull(field, "The field should not be a null or empty, should try invoke 'withField'.");

        StringBuilder indexBuilder = new StringBuilder(indexTemplatesLength.get(index) +
                collection.length() + field.length());
        List<String[]> templates = indexCreationTemplates.get(index);
        for (String[] template : templates)
            indexBuilder
                    .append(template[0])
                    .append(collection)
                    .append(template[1])
                    .append(field)
                    .append(template[2]);

        return indexBuilder.toString();
    }

    private void requiresNonnull(String str, String message) throws BuildingException {
        if (str == null || str.isEmpty())
            throw new BuildingException(message);
    }
}
