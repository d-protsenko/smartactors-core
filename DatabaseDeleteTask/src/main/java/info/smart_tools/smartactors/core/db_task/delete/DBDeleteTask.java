package info.smart_tools.smartactors.core.db_task.delete;

import info.smart_tools.smartactors.core.db_storage.DataBaseStorage;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.utils.ConnectionPool;
import info.smart_tools.smartactors.core.db_task.delete.wrappers.DeletionQuery;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.sql_commons.JDBCCompiledQuery;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

import javax.annotation.Nonnull;
import java.sql.SQLException;

public abstract class DBDeleteTask implements IDatabaseTask {
    private ConnectionPool connectionPool;

    protected DBDeleteTask(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    protected void execute(@Nonnull QueryStatement query, @Nonnull DeletionQuery message)
            throws TaskExecutionException {
        try {
            DataBaseStorage.executeTransaction(connectionPool, connection ->
                    executeDeleteQuery(connection.compileQuery(query), message));
        } catch (Exception e) {
            throw new TaskExecutionException(e.getMessage(), e);
        }
    }

    private void executeDeleteQuery(@Nonnull CompiledQuery statement, @Nonnull DeletionQuery message)
            throws QueryExecutionException {
        try {
            int nDeleted = ((JDBCCompiledQuery)statement).getPreparedStatement().executeUpdate();

            if(nDeleted != message.countDocumentIds()) {
                throw new QueryExecutionException("Delete query failed: wrong count of documents is deleted.");
            }
        } catch (SQLException e) {
            throw new QueryExecutionException("Deletion query execution failed because of SQL exception.",e);
        }
    }
}
