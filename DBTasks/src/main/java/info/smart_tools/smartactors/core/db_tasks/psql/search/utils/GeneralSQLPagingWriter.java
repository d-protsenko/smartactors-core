package info.smart_tools.smartactors.core.db_tasks.psql.search.utils;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.SQLQueryParameterSetter;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * {@see SearchQueryWriter} {@link ISearchQueryStatementWriter}.
 * General writer a PAGING(LIMIT and OFFSET) clause for psql db.
 */
public class GeneralSQLPagingWriter implements ISearchQueryStatementWriter<int[]> {
    /** Const param for paging */
    private static final int MAX_PAGE_SIZE = 10000;
    private static final int MIN_PAGE_SIZE = 1;

    private GeneralSQLPagingWriter() {}

    /**
     * Factory method for creation a new instance of <pre>GeneralSQLPagingWriter</pre>.
     *
     * @return a new instance of <pre>GeneralSQLPagingWriter</pre>.
     */
    public static GeneralSQLPagingWriter create() {
        return new GeneralSQLPagingWriter();
    }

    /**
     * Writes a PAGING(LIMIT and OFFSET) clause into the query statement.
     *
     * @param queryStatement - a compiled statement of query.
     * @param paging -
     * @param setters - list of query parameters setter.
     *                Any setter sets some parameter into query.
     *
     * @throws QueryBuildException when writing body of query error.
     */
    public void write(
            @Nonnull final QueryStatement queryStatement,
            @Nonnull final int[] paging,
            @Nonnull final List<SQLQueryParameterSetter> setters
    ) throws QueryBuildException {
        try {
            queryStatement.getBodyWriter().write("LIMIT(?)OFFSET(?)");
            setters.add((statement, index) -> {
                int pageNumber = paging[0] - 1;
                int pageSize = paging[1];

                pageNumber = (pageNumber < 0) ? 0 : pageNumber;
                pageSize = (pageSize > MAX_PAGE_SIZE) ?
                        MAX_PAGE_SIZE : ((pageSize < MIN_PAGE_SIZE) ? MIN_PAGE_SIZE : pageSize);

                statement.setInt(index++, pageSize);
                statement.setInt(index++, pageSize * pageNumber);
                return index;
            });
        } catch (IOException e) {
            throw new QueryBuildException("Error while writing PAGING clause of search query SQL.", e);
        }
    }
}
