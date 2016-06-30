package info.smart_tools.smartactors.core.db_task.upsert;


import info.smart_tools.smartactors.core.db_storage.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_task.upsert.psql.wrapper.IUpsertQueryMessage;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;

import javax.annotation.Nonnull;

/**
 * Common update task executor.
 */
public abstract class DBUpdateTask extends DBUpsert {
    /**
     *
     */
    protected DBUpdateTask() {}

    /**
     * Executes update documents query to database.
     *
     * @param query - a compiled executable query to database.
     * @param message - source message with parameters for query.
     *
     * @throws TaskExecutionException when:
     *              1. The result set size hasn't equals of number of updating documents;
     *              2. Query execution errors.
     */
    protected void execute(@Nonnull final CompiledQuery query, @Nonnull final IUpsertQueryMessage message)
            throws TaskExecutionException {
        try {
            int nUpdated = query.executeUpdate();
            if (nUpdated != 1) {
                throw new QueryExecutionException("Update query failed: wrong count of documents is updated.");
            }
        } catch (QueryExecutionException e) {
            throw new TaskExecutionException("Update query execution failed because:" + e.getMessage(), e);
        }
    }
}
