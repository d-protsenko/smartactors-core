package info.smart_tools.smartactors.core.db_tasks.commons;


import info.smart_tools.smartactors.core.db_storage.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_tasks.wrappers.update.IUpdateMessage;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;

import javax.annotation.Nonnull;

/**
 * Common update task executor.
 */
public abstract class DBUpdateTask extends GeneralDatabaseTask<IUpdateMessage> {
    /**
     *
     */
    protected DBUpdateTask() {}

    @Override
    protected boolean requiresExit(@Nonnull final IUpdateMessage queryMessage) throws InvalidArgumentException {
        try {
            return queryMessage.getDocument() == null;
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
    protected void execute(@Nonnull final CompiledQuery query, @Nonnull final IUpdateMessage message)
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
