package info.smart_tools.smartactors.core.db_task.search_by_id;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_task.search_by_id.psql.wrapper.ISearchByIdQuery;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Common search by id task executor.
 */
public abstract class DBSearchByIdTask implements IDatabaseTask {
    /**
     * Default constructor.
     */
    protected DBSearchByIdTask() {}

    /**
     * Executes search by id query of rows to database.
     *
     * @param compiledQuery - a compiled executable query to database.
     * @param message - source message with parameters for query.
     *
     * @throws TaskExecutionException when the result set has more than one document.
     */
    protected void execute(@Nonnull CompiledQuery compiledQuery, @Nonnull ISearchByIdQuery message)
            throws TaskExecutionException {
        try {
            ResultSet resultSet = compiledQuery.executeQuery();
            if (resultSet != null && resultSet.first()) {
                try {
                    message.setSearchResult((IObject) resultSet.getObject(0));
                } catch (ChangeValueException e) {
                    throw new TaskExecutionException("Could not set the document.");
                }
            } else {
                throw new TaskExecutionException("Not found document with this id.");
            }
        } catch (QueryExecutionException | SQLException e) {
            throw new TaskExecutionException("'Search by id task' execution has been failed because:", e);
        }
    }
}
