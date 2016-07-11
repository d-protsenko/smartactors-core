package info.smart_tools.smartactors.core.db_tasks.psql.search;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.sql_commons.ParamContainer;
import info.smart_tools.smartactors.core.sql_commons.QueryConditionWriterResolver;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

import javax.annotation.Nonnull;
import java.io.Writer;
import java.util.List;

/**
 *
 */
final class SearchQueryStatementBuilder {
    private String collection;
    private Object criteria;
    private List<IObject> orderByItems;
    private int[] paging = new int[2];

    private static final QueryConditionWriterResolver CONDITION_WRITER_RESOLVER  = ConditionsWriterResolver.create();

    private final static String FIRST_PART_TEMPLATE = "SELECT * FROM ";
    private final static String SECOND_PART_TEMPLATE = " WHERE";

    private final static int TEMPLATE_SIZE = FIRST_PART_TEMPLATE.length() +
            SECOND_PART_TEMPLATE.length();


    public static SearchQueryStatementBuilder create() {
        return new SearchQueryStatementBuilder();
    }

    /**
     * Appends collection name to final query statement.
     *
     * @param collection - a collection name for which to create query statement.
     *
     * @return a link to yourself {@link SearchByIdQueryStatementBuilder}.
     */
    SearchQueryStatementBuilder withCollection(@Nonnull final String collection) {
        this.collection = collection;
        return this;
    }

    /**
     *
     * @param criteria
     * @return
     */
    SearchQueryStatementBuilder withCriteria(@Nonnull final Object criteria) {
        this.criteria = criteria;
        return this;
    }

    /**
     *
     * @param orderByItems
     * @return
     */
    SearchQueryStatementBuilder withOrderByItems(@Nonnull final List<IObject> orderByItems) {
        this.orderByItems = orderByItems;
        return this;
    }

    /**
     *
     * @param pageNumber
     * @return
     */
    SearchQueryStatementBuilder withPageNumber(@Nonnull final int pageNumber) {
        this.paging[0] = pageNumber;
        return this;
    }

    /**
     *
     * @param pageSize
     * @return
     */
    SearchQueryStatementBuilder withPageSize(@Nonnull final int pageSize) {
        this.paging[1] = pageSize;
        return this;
    }


    /**
     *
     * @return
     * @throws QueryBuildException
     */
    QueryStatement build(List<ParamContainer> order) throws QueryBuildException {
        try {
            QueryStatement queryStatement = new QueryStatement();
            Writer writer = queryStatement.getBodyWriter();
            StringBuilder queryBuilder = new StringBuilder(TEMPLATE_SIZE);
            queryBuilder
                    .append(FIRST_PART_TEMPLATE)
                    .append(collection)
                    .append(SECOND_PART_TEMPLATE);
            writer.write(queryBuilder.toString());
            CONDITION_WRITER_RESOLVER
                    .resolve(null)
                    .write(queryStatement, CONDITION_WRITER_RESOLVER, null, criteria, order);
//            ORDER_WRITER.write(queryStatement, orderByItems, order);
//            PAGING_WRITER.write(queryStatement, paging, order);

            return queryStatement;
        } catch (QueryBuildException e) {
            throw new QueryBuildException("Error writing query statement: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }
}
