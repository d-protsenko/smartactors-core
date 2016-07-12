package info.smart_tools.smartactors.core.db_tasks.commons.queries;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

/**
 *
 */
public interface IQueryStatementBuilder {
    /**
     *
     * @return
     * @throws QueryBuildException
     */
    QueryStatement build() throws QueryBuildException;
}
