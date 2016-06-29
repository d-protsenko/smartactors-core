package info.smart_tools.smartactors.core.db_task.delete.psql;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_task.delete.DBDeleteTask;
import info.smart_tools.smartactors.core.db_task.delete.wrappers.IDeletionQueryMessage;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.QueryKey;
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
    /** {@see DeletionQuery} {@link IDeletionQueryMessage} */
    private IDeletionQueryMessage message;

    /**
     * A single constructor for creation {@link PSQLDeleteTask}
     *
     */
    protected PSQLDeleteTask() {}

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
            IDeletionQueryMessage messageWrapper = takeQueryMessage(deleteMessage);
            if (messageWrapper.countDocumentIds() == 0) {
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
        if (query == null || message == null) {
            throw new TaskExecutionException("Should first prepare the task.");
        }

        super.execute(query, message);
    }

    /**
     *
     * @param storageConnection - database connection.
     * @throws TaskSetConnectionException
     */
    public void setStorageConnection(@Nonnull StorageConnection storageConnection) throws TaskSetConnectionException {
        verify(storageConnection);
        this.connection = storageConnection;
    }

    private CompiledQuery takeQuery(
            String collection,
            int documentsIdsNumber,
            StorageConnection connection
    ) throws ResolutionException {
        IKey queryKey = IOC.resolve(
                Keys.getOrAdd(QueryKey.class.toString()),
                connection.getId(),
                PSQLDeleteTask.class.toString(),
                collection,
                documentsIdsNumber);

        return IOC.resolve(
                Keys.getOrAdd(CompiledQuery.class.toString() + "USED_CACHE"),
                queryKey,
                connection,
                getQueryStatementFactory(collection, documentsIdsNumber));
    }

    private IDeletionQueryMessage takeQueryMessage(IObject object) throws ResolutionException {
        return IOC.resolve(
                Keys.getOrAdd(IDeletionQueryMessage.class.toString()),
                object);
    }

    private void setInternalState(CompiledQuery query, IDeletionQueryMessage message) {
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
            } catch (QueryBuildException e) {
                throw new QueryStatementFactoryException("Error while initialize update query.", e);
            }
        };
    }

    private CompiledQuery formatQuery(final CompiledQuery query, final IDeletionQueryMessage message)
            throws QueryBuildException {

        int documentsIdsSize = message.countDocumentIds();
        query.setParameters(Collections.singletonList((statement, index) -> {
            for (int i = 0; i < documentsIdsSize; ++i) {
                statement.setLong(index++, message.getDocumentIds(i));
            }
            return index;
        }));

        return query;
    }

    private void verify(StorageConnection connection) throws TaskSetConnectionException {
        if (connection == null)
            throw new TaskSetConnectionException("Connection should not be a null or empty!");
        if (connection.getId() == null || connection.getId().isEmpty())
            throw new TaskSetConnectionException("Connection should have an id!");
    }
}