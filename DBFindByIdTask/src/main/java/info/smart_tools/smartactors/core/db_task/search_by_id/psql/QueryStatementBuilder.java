package info.smart_tools.smartactors.core.db_task.search_by_id.psql;

import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Builder for query statement {@link QueryStatement} of find by id query.
 */
class QueryStatementBuilder {
    private String collection;
    private String id;

    private final static String FIRST_PART_TEMPLATE = "SELECT * FROM ";
    private final static String SECOND_PART_TEMPLATE = " WHERE token = ";
    private final static String THIRD_PART_TEMPLATE = ";";

    private final static int TEMPLATE_SIZE = FIRST_PART_TEMPLATE.length() +
            SECOND_PART_TEMPLATE.length() + THIRD_PART_TEMPLATE.length();

    /**
     * Default constructor. Creates a new instance of {@link QueryStatementBuilder}.
     */
    private QueryStatementBuilder() {}

    /**
     * Factory method for creates a new instance of {@link QueryStatementBuilder}.
     *
     * @return a new instance of {@link QueryStatementBuilder}.
     */
    static QueryStatementBuilder create() { return new QueryStatementBuilder(); }

    /**
     * Appends collection name to final query.
     *
     * @param collection - a collection name for which to create query statement.
     *
     * @return a link to yourself {@link QueryStatementBuilder}.
     */
    QueryStatementBuilder withCollection(@Nonnull String collection) {
        this.collection = collection;
        return this;
    }

    /**
     * Appends id document which needs find.
     *
     * @param id - document's id which needs find.
     *
     * @return a link to yourself {@link QueryStatementBuilder}.
     */
    QueryStatementBuilder withId(@Nonnull String id) {
        this.id = id;
        return this;
    }

    /**
     * Builds query statement of find by id query.
     *
     * @return formed and filled query statement {@link QueryStatement}.
     *
     * @throws BuildingException when a some critical error in during building query statement.
     */
    QueryStatement build() throws BuildingException {
        requiresNonnull(collection, "The collection should not be a null or empty, should try invoke 'withCollection'.");
        requiresNonnull(id, "The id should not be a null or empty, should try invoke 'withId'.");

        try {
            QueryStatement preparedQuery = IOC.resolve(Keys.getOrAdd(QueryStatement.class.toString()));
            StringBuilder queryBuilder = new StringBuilder(TEMPLATE_SIZE + collection.length() + id.length());
            preparedQuery.getBodyWriter().write(queryBuilder
                    .append(FIRST_PART_TEMPLATE)
                    .append(collection)
                    .append(SECOND_PART_TEMPLATE)
                    .append(id)
                    .append(THIRD_PART_TEMPLATE)
                    .toString());

            return preparedQuery;
        } catch (IOException | ResolutionException e) {
            throw new BuildingException(e.getMessage(), e);
        }
    }

    private void requiresNonnull(String str, String message) throws BuildingException {
        if (str == null || str.isEmpty())
            throw new BuildingException(message);
    }
}
