package info.smart_tools.smartactors.core.db_task.search.utils;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_task.search.wrappers.SearchQuery;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

import javax.annotation.Nonnull;

/**
 * Writer for a some clause of a searching query.
 */
public interface SearchQueryWriter {
    /**
     * Writes a some clause in the searching query.
     *
     * @param queryStatement - statement of the searching query.
     * @param queryMessage - message with parameters for the searching query.
     *
     * @throws QueryBuildException during query writing.
     */
    void write(@Nonnull final QueryStatement queryStatement, @Nonnull final SearchQuery queryMessage)
            throws QueryBuildException;
}
