package info.smart_tools.smartactors.core.db_task.create_collection.psql;

import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.FieldPath;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder for query statement {@link QueryStatement} of create collection query.
 */
class QueryStatementBuilder {
    private String collection;
    private String createCollectionQuery;
    private Map<String, String> indexes;
    private IndexBuilder indexBuilder;

    private final static String FIRST_PART_TEMPLATE = "CREATE TABLE ";
    private final static String SECOND_PART_TEMPLATE = " (id BIGSERIAL PRIMARY KEY, document JSONB NOT NULL);\n";
    private final static int TEMPLATE_SIZE = FIRST_PART_TEMPLATE.length() + SECOND_PART_TEMPLATE.length();

    /**
     * Default constructor for {@link QueryStatementBuilder}.
     *              Creates a new instance of {@link QueryStatementBuilder}.
     */
    private QueryStatementBuilder() {
        this.indexBuilder = IndexBuilder.create();
    }

    /**
     * Factory method for creation a new instance of {@link QueryStatementBuilder}.
     *
     * @return a new instance of {@link QueryStatementBuilder}.
     */
    static QueryStatementBuilder create() {
        return new QueryStatementBuilder();
    }

    /**
     * Appends collection name to final query statement.
     *
     * @param collection - a collection name for which to create query statement.
     *
     * @return a link to yourself {@link QueryStatementBuilder}.
     */
    QueryStatementBuilder withCollection(@Nonnull String collection) {
        this.collection = collection;

        StringBuilder createCollectionQueryBuilder =
                new StringBuilder(TEMPLATE_SIZE + collection.length());
        createCollectionQuery = createCollectionQueryBuilder
                .append(FIRST_PART_TEMPLATE)
                .append(collection)
                .append(SECOND_PART_TEMPLATE)
                .toString();

        return this;
    }

    /**
     * Appends list of indexes to some collection.
     *
     * @param indexes - list of indexes to some collection.
     *
     * @return a link to yourself {@link QueryStatementBuilder}.
     */
    QueryStatementBuilder withIndexes(@Nonnull Map<String, String> indexes) {
        this.indexes = new HashMap<>(indexes);
        if (this.indexes.containsKey("id")) {
            this.indexes.put("id","id");
        }

        return this;
    }

    /**
     * Builds query statement of create collection query.
     *
     * @return formed and filled query statement {@link QueryStatement}.
     *
     * @throws BuildingException when a some critical error in during building query statement.
     */
    QueryStatement build() throws BuildingException {
        requiresNonnull(collection, "The collection should not be a null or empty, should try invoke 'withCollection'.");
        requiresNonnull(indexes, "The list of indexes should not be a null, should try invoke 'withIndexes'.");

        try {
            QueryStatement preparedQuery = IOC.resolve(Keys.getOrAdd(QueryStatement.class.toString()));
            Writer writer = preparedQuery.getBodyWriter();

            writer.write(createCollectionQuery);
            indexBuilder.withCollection(collection);
            for (Map.Entry<String, String> entry : indexes.entrySet()) {
                FieldPath field = IOC.resolve(Keys.getOrAdd(FieldPath.class.toString()), entry.getKey());
                String indexType = entry.getValue();
                String index = indexBuilder
                        .withIndex(indexType)
                        .withField(field.getSQLRepresentation())
                        .build();

                writer.write(index);
            }

            return preparedQuery;
        } catch (ResolutionException | IOException | IllegalArgumentException e) {
            throw new BuildingException(e.getMessage(), e);
        }
    }

    private void requiresNonnull(String str, String message) throws BuildingException {
        if (str == null || str.isEmpty())
            throw new BuildingException(message);
    }

    private void requiresNonnull(Map map, String message) throws BuildingException {
        if (map == null)
            throw new BuildingException(message);
    }
}
