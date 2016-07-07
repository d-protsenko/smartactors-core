package info.smart_tools.smartactors.core.db_tasks.psql.search.utils;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.SQLQueryParameterSetter;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Writer for a some clause of a searching query.
 */
public interface ISearchQueryStatementWriter<TSetParameter> {
    /**
     * Writes a some clause in the searching query.
     *
     * @param queryStatement - statement of the searching query.
     * @param setParameter -
     * @param setters - list of query parameters setter.
     *                Any setter sets some parameter into query.
     *
     * @throws QueryBuildException during query writing.
     */
    void write(
            @Nonnull final QueryStatement queryStatement,
            @Nonnull final TSetParameter setParameter,
            @Nonnull final List<SQLQueryParameterSetter> setters) throws QueryBuildException;
}
