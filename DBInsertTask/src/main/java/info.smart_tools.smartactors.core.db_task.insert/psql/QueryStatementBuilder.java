package info.smart_tools.smartactors.core.db_task.insert.psql;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;

/**
 *
 */
final class QueryStatementBuilder {
    private String collection;
    private int documentsNumber;

    private static final String FIRST_PART_TEMPLATE = "INSERT ";
    private static final String SECOND_PART_TEMPLATE = "AS tab SET document = docs.document FROM (VALUES";
    private static final String THIRD_PART_TEMPLATE = "(?::jsonb)";
    private static final String FOURTH_PART_TEMPLATE = " RETURNING id;";

    private static final int TEMPLATE_SIZE = FIRST_PART_TEMPLATE.length() +
            SECOND_PART_TEMPLATE.length() + FOURTH_PART_TEMPLATE.length();

    /**
     *
     */
    private QueryStatementBuilder() {}

    /**
     *
     * @return
     */
    static QueryStatementBuilder create() {
        return new QueryStatementBuilder();
    }

    /**
     *
     * @param collection
     * @return
     */
    QueryStatementBuilder withCollection(@Nonnull final String collection) {
        this.collection = collection;
        return this;
    }

    /**
     *
     * @param documentsNumber
     * @return
     */
    QueryStatementBuilder withDocumentsNumber(final int documentsNumber) {
        validateDocumentsNumber(documentsNumber);
        this.documentsNumber = documentsNumber;
        return this;
    }

    /**
     *
     * @return
     * @throws QueryBuildException
     */
    QueryStatement build() throws QueryBuildException {
        try {
            requiresNonnull(collection, "The collection should not be a null or empty, should try invoke 'withCollection'.");
            validateDocumentsNumber(documentsNumber);

            QueryStatement preparedQuery = IOC.resolve(Keys.getOrAdd(QueryStatement.class.toString()));
            Writer writer = preparedQuery.getBodyWriter();
            StringBuilder queryBuilder = new StringBuilder(TEMPLATE_SIZE + collection.length() +
                    documentsNumber * THIRD_PART_TEMPLATE.length() + (documentsNumber - 1));

            queryBuilder
                    .append(FIRST_PART_TEMPLATE)
                    .append(collection)
                    .append(SECOND_PART_TEMPLATE);
            for (int i = 0; i < documentsNumber; ++i) {
                queryBuilder
                        .append(THIRD_PART_TEMPLATE)
                        .append((i == 1) ? "" : ",");
            }
            queryBuilder.append(FOURTH_PART_TEMPLATE);
            writer.write(queryBuilder.toString());

            return preparedQuery;
        } catch (IOException | ResolutionException | IllegalArgumentException e) {
            throw new QueryBuildException("A query statement building error: " + e.getMessage(), e);
        }
    }

    private void validateDocumentsNumber(final int number) {
        if (number <= 0) {
            throw new IllegalArgumentException("The documents number should be a more than zero.");
        }
    }

    private void requiresNonnull(final String str, final String message) throws QueryBuildException {
        if (str == null || str.isEmpty()) {
            throw new QueryBuildException(message);
        }
    }
}
