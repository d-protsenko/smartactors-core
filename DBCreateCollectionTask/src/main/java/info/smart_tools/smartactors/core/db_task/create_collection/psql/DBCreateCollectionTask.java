package info.smart_tools.smartactors.core.db_task.create_collection.psql;

import com.sun.istack.internal.NotNull;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.core.db_storage.exceptions.StorageException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_task.create_collection.psql.wrapper.ICreateCollectionQuery;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

import java.util.Map;

/**
 * Task for create collection with predefined indexes.
 */
public class DBCreateCollectionTask implements IDatabaseTask {
    private CompiledQuery query;
    private StorageConnection connection;

    /**
     * Default constructor.
     *              Creates a new instance of {@link DBCreateCollectionTask}.
     */
    private DBCreateCollectionTask() {}

    /**
     * Factory method for creation a new instance of {@link DBCreateCollectionTask}.
     *
     * @return a new instance of {@link DBCreateCollectionTask}.
     */
    public static DBCreateCollectionTask create() {
        return new DBCreateCollectionTask();
    }

    /**
     * Prepares a create collection query for execution.
     *
     * @param createCollectionMessage - {@see CreateCollectionQuery} {@link ICreateCollectionQuery}
     *                a message with parameters for prepares create a collection query.
     *
     * @throws TaskPrepareException when error resolution a some object using IOC.
     */
    @Override
    public void prepare(@NotNull IObject createCollectionMessage) throws TaskPrepareException {
        try {
            verify(connection);
            ICreateCollectionQuery messageWrapper = takeQueryMessage(createCollectionMessage);
            CompiledQuery query = takeQuery(
                    messageWrapper.getCollectionName().toString(),
                    messageWrapper.getIndexes(),
                    connection);
            setInternalState(query);
        } catch (BuildingException | StorageException | ResolutionException |
                ReadValueException | TaskSetConnectionException e) {
            throw new TaskPrepareException("'Create collection task' preparation has been failed because:", e);
        }
    }

    /**
     * Executes of the create collection query. Query should be a prepared before execute.
     *
     * @throws TaskExecutionException when error executes of the create collection query.
     */
    @Override
    public void execute() throws TaskExecutionException {
        if (query == null)
            throw new TaskExecutionException("Should first prepare the task.");

        try {
            query.execute();
        } catch (QueryExecutionException e) {
            throw new TaskExecutionException("'Create collection task' execution has been failed because: ", e);
        }
    }

    /**
     * Setter for connection.
     */
    @Override
    public void setConnection(@NotNull StorageConnection connection) throws TaskSetConnectionException {
        verify(connection);
        this.connection = connection;
    }

    private CompiledQuery takeQuery(String collection, Map<String, String> indexes, StorageConnection connection)
            throws BuildingException, StorageException {
        QueryStatement queryStatement = QueryStatementBuilder
                .create()
                .withCollection(collection)
                .withIndexes(indexes)
                .build();

        return connection.compileQuery(queryStatement);
    }

    private ICreateCollectionQuery takeQueryMessage(IObject object) throws ResolutionException {
        return IOC.resolve(
                Keys.getOrAdd(ICreateCollectionQuery.class.toString()),
                object);
    }

    private void setInternalState(CompiledQuery query) {
        this.query = query;
    }

    private void verify(StorageConnection connection) throws TaskSetConnectionException {
        if (connection == null)
            throw new TaskSetConnectionException("Connection should not be a null or empty!");
        if (connection.getId() == null || connection.getId().isEmpty())
            throw new TaskSetConnectionException("Connection should have an id!");
    }
}
