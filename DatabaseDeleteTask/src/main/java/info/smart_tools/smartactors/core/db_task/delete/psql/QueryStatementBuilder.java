package info.smart_tools.smartactors.core.db_task.delete.psql;

import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;

/**
 * Builder for query statement {@link QueryStatement} of delete document by id query.
 */
class QueryStatementBuilder {
    private String collection;
    private int idsNumber;

    private final static String FIRST_PART_TEMPLATE = "DELETE FROM ";
    private final static String SECOND_PART_TEMPLATE = " WHERE id IN (";
    private final static int TEMPLATE_SIZE = FIRST_PART_TEMPLATE.length() + SECOND_PART_TEMPLATE.length();

    /**
     * Default constructor. Creates a new instance of {@link QueryStatementBuilder}.
     */
    private QueryStatementBuilder() {}

    /**
     * Factory method for creates a new instance of {@link QueryStatementBuilder}.
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
        return this;
    }

    /**
     * Appends ids number for creation parameters in final query statement.
     *
     * @param idsNumber - a number of documents ids which needs to delete.
     *
     * @return a link to yourself {@link QueryStatementBuilder}.
     */
    QueryStatementBuilder withIdsNumber(int idsNumber) {
        this.idsNumber = idsNumber;
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

        try {
            QueryStatement preparedQuery = IOC.resolve(Keys.getOrAdd(QueryStatement.class.toString()));
            Writer writer = preparedQuery.getBodyWriter();
            StringBuilder queryBuilder = new StringBuilder(TEMPLATE_SIZE + collection.length() + (idsNumber * 2 + 1));

            queryBuilder
                    .append(FIRST_PART_TEMPLATE)
                    .append(collection)
                    .append(SECOND_PART_TEMPLATE);
            for (int i = idsNumber; i > 0; --i) {
                queryBuilder
                        .append("?")
                        .append((i == 1) ? ");" : ",");
            }
            writer.write(queryBuilder.toString());

            return preparedQuery;
        } catch (ResolutionException | IOException e) {
            throw new BuildingException(e.getMessage(), e);
        }
    }

    private void requiresNonnull(String str, String message) throws BuildingException {
        if (str == null || str.isEmpty())
            throw new BuildingException(message);
    }
}
