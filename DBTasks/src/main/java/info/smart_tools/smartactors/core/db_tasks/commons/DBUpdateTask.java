package info.smart_tools.smartactors.core.db_tasks.commons;


import info.smart_tools.smartactors.core.db_storage.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;

import javax.annotation.Nonnull;

/**
 * Common update task executor.
 */
public abstract class DBUpdateTask extends CachedDatabaseTask {
    /**
     *
     */
    protected DBUpdateTask() {}

    @Override
    protected boolean requiresExecutable(@Nonnull final IObject message) throws InvalidArgumentException {
        try {
            return DBQueryFields.DOCUMENT.in(message) != null;
        } catch (ReadValueException e) {
            throw new InvalidArgumentException(e.getMessage(), e);
        }
    }

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
    @Override
    protected void execute(@Nonnull final ICompiledQuery query, @Nonnull final IObject message)
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
