package info.smart_tools.smartactors.core.db_tasks.commons;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;

import javax.annotation.Nonnull;

/**
 * Common deletion task executor.
 */
public abstract class DBDeleteTask extends CachedDatabaseTask {

    /**
     * Default constructor.
     */
    protected DBDeleteTask() {}

    @Override
    protected boolean requiresExecutable(@Nonnull final IObject message) throws InvalidArgumentException {
        try {
            return DBQueryFields.DOCUMENT_ID.in(message) != null;
        } catch (ReadValueException e) {
            throw new InvalidArgumentException(e.getMessage(), e);
        }
    }

    /**
     * Executes deletion query of rows to database.
     *
     * @param query - a compiled executable query to database.
     * @param message - source message with parameters for query.
     *
     * @throws TaskExecutionException when number of deleted rows not equals of number of rows which had to be removed.
     */
    @Override
    protected void execute(@Nonnull final ICompiledQuery query, @Nonnull final IObject message)
            throws TaskExecutionException {
        try {
            int nDeleted = query.executeUpdate();
            if (nDeleted != 1) {
                throw new TaskExecutionException("'Delete task' has been failed: wrong count of documents is deleted.");
            }
        } catch (QueryExecutionException e) {
            throw new TaskExecutionException("'Delete task' execution has been failed because:" + e.getMessage(), e);
        }
    }
}
