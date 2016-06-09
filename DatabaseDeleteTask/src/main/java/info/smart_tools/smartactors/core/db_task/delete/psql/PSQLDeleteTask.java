package info.smart_tools.smartactors.core.db_task.delete.psql;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.db_storage.utils.ConnectionPool;
import info.smart_tools.smartactors.core.db_task.delete.DBDeleteTask;
import info.smart_tools.smartactors.core.db_task.delete.DeleteTaskException;
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

/**
 * Task for deletion documents from database.
 */
public class PSQLDeleteTask extends DBDeleteTask {
    private QueryStatement query;
    private DeletionQuery message;

    /**
     * A single constructor for creation {@link PSQLDeleteTask}
     *
     * @param connectionPool container for all connections to database.
     * @param collectionName the name of collection to which a query is sent.
     *
     * @throws DeleteTaskException when error writing deletion query statement.
     */
    private PSQLDeleteTask(ConnectionPool connectionPool, CollectionName collectionName) throws DeleteTaskException {
        super(connectionPool);
        try {
            query = new QueryStatement();
            query.getBodyWriter().write(String.format("DELETE FROM %s WHERE %s IN (",
                    collectionName.toString(), "id"));
        } catch (IOException e) {
            throw new DeleteTaskException("Error while writing deletion query statement.", e);
        }
    }

    /**
     * Factory method for creation new instance of {@link PSQLDeleteTask}.
     *
     * @param connectionPool container for all connections to database.
     * @param collectionName the name of collection to which a query is sent.
     *
     * @return a new {@link PSQLDeleteTask} object.
     *
     * @throws DeleteTaskException when invalid collection name.
     */
    public static PSQLDeleteTask create(@Nonnull ConnectionPool connectionPool, @Nonnull String collectionName)
            throws DeleteTaskException {
        try {
            CollectionName cName = CollectionName.fromString(collectionName);
            return new PSQLDeleteTask(connectionPool, cName);
        } catch (QueryBuildException e) {
            throw new DeleteTaskException(e.getMessage(), e);
        }
    }

    /**
     * {@see IDatabaseTask} {@link info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask}
     * Prepare query for deletion documents from database by ids.
     *
     * @param message contains parameters for deletion query.
     *                Should contains list of documents ids deleted objects.
     *
     * @throws TaskPrepareException when:
     *                1. IOC resolution error;
     *                2. List of documents ids is null or empty;
     *                3. Error writing deletion query statement.
     */
    @Override
    public void prepare(@Nonnull IObject message) throws TaskPrepareException {
        try {
            this.message = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), DeletionQuery.class),
                    message
            );
        } catch (ResolutionException e) {
            throw new TaskPrepareException(e.getMessage(), e);
        }

        if(this.message.countDocumentIds() == 0) {
            throw new TaskPrepareException("List of id's to delete should not be empty.");
        }

        try {
            int documentsIdsSize = this.message.countDocumentIds();
            Writer writer = query.getBodyWriter();
            for (int i = documentsIdsSize; i > 0; --i) {
                writer.write("?" + ((i == 1) ? "" : ","));
            }
            writer.write(")");
            query.pushParameterSetter((statement, index) -> {
                for (int i = 0; i < documentsIdsSize; ++i)
                    statement.setLong(index++, this.message.getDocumentIds(i));

                return index;
            });
        } catch (IOException e) {
            throw new TaskPrepareException("Error while writing deletion query statement.", e);
        }
    }

    /**
     * {@see ITask} {@link info.smart_tools.smartactors.core.itask.ITask}
     * Executes deletion documents by ids from database.
     *
     * @throws TaskExecutionException when error in execution process.
     */
    @Override
    public void execute() throws TaskExecutionException {
        super.execute(query, message);
    }
}