package delete.psql;


import delete.psql.wrappers.DeletionQuery;
import delete.utils.CollectionName;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.utils.ConnectionPool;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.sql_commons.JDBCCompiledQuery;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;

public class DBDeleteTask implements IDatabaseTask {
    private ConnectionPool connectionPool;

    private QueryStatement query;
    private DeletionQuery message;

    private DBDeleteTask(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public static DBDeleteTask create(@Nonnull ConnectionPool connectionPool) {
        return new DBDeleteTask(connectionPool);
    }

    @Override
    public void prepare(final IObject message) throws TaskPrepareException {
        DeletionQuery taskMessage;
        try {
            this.message = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), DeletionQuery.class, message));
            taskMessage = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), DeletionQuery.class),
                    message
            );
        } catch (ResolutionException e) {
            throw new TaskPrepareException(e.getMessage(), e);
        }

        query = new QueryStatement();
        CollectionName collectionName = CollectionName.fromString(taskMessage.getCollectionName());

        if(taskMessage.countDocumentIds() == 0) {
            throw new TaskPrepareException("List of id's to delete should not be empty.");
        }

        try {
            Writer writer = query.getBodyWriter();

            writer.write(String.format("DELETE FROM %s WHERE %s IN (",
                    collectionName.toString(), "id"));

            for (int i = taskMessage.countDocumentIds(); i > 0; --i) {
                writer.write("?" + ((i == 1) ? "" : ","));
            }

            writer.write(")");

            query.pushParameterSetter((statement, index) -> {
                for (int i = 0; i < taskMessage.countDocumentIds(); ++i)
                    statement.setLong(index++, taskMessage.getDocumentIds(i));

                return index;
            });
        } catch (IOException e) {
            throw new TaskPrepareException("Error while writing deletion query statement.",e);
        }
    }

    @Override
    public void execute() throws TaskExecutionException {

    }

    public void executeDeleteQuery(CompiledQuery statement, DeletionQuery message)
            throws TaskExecutionException {
        try {
            int nDeleted = ((JDBCCompiledQuery)statement).getPreparedStatement().executeUpdate();

            if(nDeleted != message.countDocumentIds()) {
                throw new TaskExecutionException("Delete query failed: wrong count of documents is deleted.");
            }
        } catch (SQLException e) {
            throw new TaskExecutionException("Deletion query execution failed because of SQL exception.",e);
        }
    }
}
