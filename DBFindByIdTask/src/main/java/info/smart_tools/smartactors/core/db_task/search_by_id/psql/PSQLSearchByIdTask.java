package info.smart_tools.smartactors.core.db_task.search_by_id.psql;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_task.search_by_id.DBSearchByIdTask;
import info.smart_tools.smartactors.core.db_task.search_by_id.wrapper.ISearchByIdQueryMessage;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.QueryKey;
import info.smart_tools.smartactors.core.sql_commons.QueryStatementFactory;
import info.smart_tools.smartactors.core.sql_commons.exception.QueryStatementFactoryException;

import javax.annotation.Nonnull;
import java.util.Collections;

/**
 * Database task for search documents by id
 */
public class PSQLSearchByIdTask extends DBSearchByIdTask {
    private StorageConnection connection;
    private CompiledQuery query;
    private ISearchByIdQueryMessage message;

    /**
     * Constructor for DBGetByIdTask
     */
    protected PSQLSearchByIdTask() {}

    /**
     * Factory method for creation a new instance of {@link PSQLSearchByIdTask}.
     *
     * @return a new instance of {@link PSQLSearchByIdTask}.
     */
    public static PSQLSearchByIdTask create() {
        return new PSQLSearchByIdTask();
    }

    /**
     * Prepares a search by id query for execution.
     *
     * @param searchByIdMessage {@see SearchByIdQuery} {@link ISearchByIdQueryMessage}
     *                a message with parameters for prepares search by id query.
     *
     * @throws TaskPrepareException when error resolution a some object using IOC or query building error.
     */
    @Override
    public void prepare(@Nonnull IObject searchByIdMessage) throws TaskPrepareException {
        try {
            verify(connection);
            ISearchByIdQueryMessage messageWrapper = takeQueryMessage(searchByIdMessage);
            CompiledQuery query = takeQuery(
                    messageWrapper.getCollectionName().toString(),
                    connection);
            setInternalState(formatQuery(query, messageWrapper), messageWrapper);
        } catch (QueryBuildException | ResolutionException | ReadValueException | TaskSetConnectionException e) {
            throw new TaskPrepareException("'Search by id task' preparation has been failed because:", e);
        }
    }

    /**
     * Executes of the search by id query. Query should be a prepared before execute.
     *
     * @throws TaskExecutionException when task didn't prepare before execute or
     *                  error executes of the create collection query.
     */
    @Override
    public void execute() throws TaskExecutionException {
        if (query == null || message == null)
            throw new TaskExecutionException("Should first prepare the task.");

        super.execute(query, message);
    }

    /**
     * Setter for storageConnection.
     */
    public void setStorageConnection(@Nonnull StorageConnection storageConnection) throws TaskSetConnectionException {
        verify(storageConnection);
        this.connection = storageConnection;
    }

    private CompiledQuery takeQuery(String collection, StorageConnection connection) throws ResolutionException {
        IKey queryKey = IOC.resolve(
                Keys.getOrAdd(QueryKey.class.toString()),
                connection.getId(),
                PSQLSearchByIdTask.class.toString(),
                collection);

        return IOC.resolve(
                Keys.getOrAdd(CompiledQuery.class.toString() + "USED_CACHE"),
                queryKey,
                connection,
                getQueryStatementFactory(collection));
    }

    private ISearchByIdQueryMessage takeQueryMessage(IObject object) throws ResolutionException {
        return IOC.resolve(
                Keys.getOrAdd(ISearchByIdQueryMessage.class.toString()),
                object);
    }

    private void setInternalState(CompiledQuery query, ISearchByIdQueryMessage message) {
        this.query = query;
        this.message = message;
    }

    private QueryStatementFactory getQueryStatementFactory(String collection) {
        return () -> {
            try {
                return QueryStatementBuilder
                        .create()
                        .withCollection(collection)
                        .build();
            } catch (QueryBuildException e) {
                throw new QueryStatementFactoryException("Error while initialize a search by id query.", e);
            }
        };
    }

    private CompiledQuery formatQuery(CompiledQuery query, ISearchByIdQueryMessage queryMessage) throws QueryBuildException {
        try {
            String id = queryMessage.getId();
            query.setParameters(Collections.singletonList((statement, index) -> {
                statement.setObject(index++, id);
                return index;
            }));

            return query;
        } catch (ReadValueException e) {
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