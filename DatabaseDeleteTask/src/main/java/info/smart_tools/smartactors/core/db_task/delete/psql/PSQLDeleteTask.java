package info.smart_tools.smartactors.core.db_task.delete.psql;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_task.delete.DBDeleteTask;
import info.smart_tools.smartactors.core.db_task.delete.wrappers.IDeletionQuery;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.QueryStatementFactory;
import info.smart_tools.smartactors.core.sql_commons.exception.QueryStatementFactoryException;

import javax.annotation.Nonnull;
import java.util.Collections;

/**
 * Task for deletion documents from database.
 */
public class PSQLDeleteTask extends DBDeleteTask {
    /** Current connection to database. */
    private StorageConnection connection;
    /** Compiled deletion query. */
    private CompiledQuery query;
    /** {@see DeletionQuery} {@link IDeletionQuery} */
    private IDeletionQuery message;

    /**
     * A single constructor for creation {@link PSQLDeleteTask}
     *
     */
    private PSQLDeleteTask() {}

    /**
     * Factory method for creation new instance of {@link PSQLDeleteTask}.
     */
    public static PSQLDeleteTask create() {
        return new PSQLDeleteTask();
    }

    /**
     * {@see IDatabaseTask} {@link info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask}
     * Prepare query for deletion documents from database by ids.
     *
     * @param deleteMessage contains parameters for deletion query.
     *                Should contains list of documents ids deleted objects.
     *
     * @throws TaskPrepareException when:
     *                1. IOC resolution error;
     *                2. List of documents ids is null or empty;
     *                3. Error writing deletion query statement.
     */
    @Override
    public void prepare(@Nonnull IObject deleteMessage) throws TaskPrepareException {
        try {
            verify(connection);
            IDeletionQuery messageWrapper = takeQueryMessage(deleteMessage);
            if(messageWrapper.countDocumentIds() == 0) {
                setInternalState(null, messageWrapper);
                return;
            }
            CompiledQuery compiledQuery = takeQuery(
                    messageWrapper.getCollectionName().toString(),
                    messageWrapper.countDocumentIds(),
                    connection);
            setInternalState(formatQuery(compiledQuery, messageWrapper), messageWrapper);
        } catch (ResolutionException | QueryBuildException | TaskSetConnectionException e) {
            throw new TaskPrepareException("'Delete task' preparation has been failed because: " + e.getMessage(), e);
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
        if (message != null && message.countDocumentIds() == 0) return;
        if (query == null || message == null)
            throw new TaskExecutionException("Should first prepare the task.");

        super.execute(query, message);
    }

    @Override
    public void setConnection(@Nonnull StorageConnection connection) throws TaskSetConnectionException {
        verify(connection);
        this.connection = connection;
    }

    private CompiledQuery takeQuery(
            String collection,
            int documentsIdsNumber,
            StorageConnection connection
    ) throws ResolutionException {
        return IOC.resolve(
                Keys.getOrAdd(CompiledQuery.class.toString()),
                connection,
                PSQLDeleteTask.class.toString(),
                getQueryStatementFactory(collection, documentsIdsNumber));
    }

    private IDeletionQuery takeQueryMessage(IObject object) throws ResolutionException {
        return IOC.resolve(
                Keys.getOrAdd(IDeletionQuery.class.toString()),
                object);
    }

    private void setInternalState(CompiledQuery query, IDeletionQuery message) {
        this.query = query;
        this.message = message;
    }

    private QueryStatementFactory getQueryStatementFactory(String collection, int documentsIdsNumber) {
        return () -> {
            try {
                return QueryStatementBuilder
                        .create()
                        .withCollection(collection)
                        .withIdsNumber(documentsIdsNumber)
                        .build();
            } catch (BuildingException e) {
                throw new QueryStatementFactoryException("Error while initialize update query.", e);
            }
        };
    }

    private CompiledQuery formatQuery(CompiledQuery compiledQuery, final IDeletionQuery queryMessage)
            throws QueryBuildException {
        try {
            int documentsIdsSize = queryMessage.countDocumentIds();
            compiledQuery.setParameters(Collections.singletonList((statement, index) -> {
                for (int i = 0; i < documentsIdsSize; ++i)
                    statement.setLong(index++, queryMessage.getDocumentIds(i));
                return index;
            }));

            return compiledQuery;
        } catch (QueryBuildException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }

    private void verify(StorageConnection connection) throws TaskSetConnectionException {
        if (connection == null)
            throw new TaskSetConnectionException("Connection should not be a null or empty!");
        if (connection.getId() == null || connection.getId().isEmpty())
            throw new TaskSetConnectionException("Connection should have an id!");
    }
}