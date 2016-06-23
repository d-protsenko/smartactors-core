package info.smart_tools.smartactors.core.db_task.delete;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_task.delete.wrappers.IDeletionQuery;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;

import javax.annotation.Nonnull;

/**
 * Common deletion task executor.
 */
public abstract class DBDeleteTask implements IDatabaseTask {

    /**
     * Default constructor.
     */
    protected DBDeleteTask() {}

    /**
     * Executes deletion query of rows to database.
     *
     * @param query - a compiled executable query to database.
     * @param message - source message with parameters for query.
     *
     * @throws TaskExecutionException when number of deleted rows not equals of number of rows which had to be removed.
     */
    protected void execute(@Nonnull CompiledQuery query, @Nonnull IDeletionQuery message)
            throws TaskExecutionException {
        try {
            int nDeleted = query.executeUpdate();
            if(nDeleted != message.countDocumentIds())
                throw new TaskExecutionException("'Delete task' has been failed: wrong count of documents is deleted.");
        } catch (QueryExecutionException e) {
            throw new TaskExecutionException("'Delete task' execution has been failed because:", e);
        }
    }
}
