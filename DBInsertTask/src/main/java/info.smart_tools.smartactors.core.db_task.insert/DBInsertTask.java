package info.smart_tools.smartactors.core.db_task.insert;

import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;

import javax.annotation.Nonnull;
import java.sql.ResultSet;

/**
 * Common insert task executor.
 */
public abstract class DBInsertTask implements IDatabaseTask {
    /**
     * Default constructor.
     */
    protected DBInsertTask() {}

    /**
     * Executes search by id query of rows to database.
     *
     * @param compiledQuery - a compiled executable query to database.
     *
     * @throws TaskExecutionException when the result set has more than one document.
     */
    protected void execute(@Nonnull CompiledQuery compiledQuery)
            throws TaskExecutionException {
        try {
            ResultSet resultSet = compiledQuery.executeQuery();
            if (resultSet == null || !resultSet.first()){
                throw new TaskExecutionException("Query execution has been failed: " +
                        "Database returned not enough generated ids");
            }
        } catch (Exception e) {
            throw new TaskExecutionException("'Insert query' execution has been failed: ", e);
        }
    }
}
