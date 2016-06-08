package info.smart_tools.smartactors.core.db_task.delete.psql;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.db_storage.utils.ConnectionPool;
import info.smart_tools.smartactors.core.db_task.delete.DBDeleteTask;
import info.smart_tools.smartactors.core.db_task.delete.wrappers.DeletionQuery;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;

public class PSQLDeleteTask extends DBDeleteTask {
    private QueryStatement query;
    private DeletionQuery message;

    // ToDo : Create common to all tasks exception.
    private PSQLDeleteTask(ConnectionPool connectionPool, CollectionName collectionName) {
        super(connectionPool);
        try {
            query = new QueryStatement();
            query.getBodyWriter().write(String.format("DELETE FROM %s WHERE %s IN (",
                    collectionName.toString(), "id"));
        } catch (IOException e) {
            // ToDo : handle exception.
            //("Error while writing deletion query statement.", e);
        }
    }

    // ToDo : Create common to all tasks exception.
    public static PSQLDeleteTask create(@Nonnull ConnectionPool connectionPool, @Nonnull String collectionName) {
        try {
            CollectionName cName = CollectionName.fromString(collectionName);
            return new PSQLDeleteTask(connectionPool, cName);
        } catch (QueryBuildException e) {
            // ToDo : handle exception.
            //(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void prepare(@Nonnull IObject message) throws TaskPrepareException {
        DeletionQuery taskMessage;
        try {
            taskMessage = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), DeletionQuery.class),
                    message
            );
            this.message = taskMessage;

            query = new QueryStatement();

        } catch (ResolutionException e) {
            throw new TaskPrepareException(e.getMessage(), e);
        }

        if(taskMessage.countDocumentIds() == 0) {
            throw new TaskPrepareException("List of id's to delete should not be empty.");
        }

        try {
            Writer writer = query.getBodyWriter();
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
            throw new TaskPrepareException("Error while writing deletion query statement.", e);
        }
    }

    @Override
    public void execute() throws TaskExecutionException {
        super.execute(query, message);
    }
}
