package info.smart_tools.smartactors.core.db_task.create_collection.psql;

import com.sun.istack.internal.NotNull;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_task.create_collection.psql.wrapper.CreateCollectionQuery;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.QueryStatementFactory;
import info.smart_tools.smartactors.core.sql_commons.exception.QueryStatementFactoryException;

/**
 * Task for create collection with predefined indexes.
 */
public class DBCreateCollectionTask implements IDatabaseTask {
    private CompiledQuery compiledQuery;
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
     * @param createCollectionMessage - {@see CreateCollectionQuery} {@link CreateCollectionQuery}
     *                a message with parameters for prepares create a collection query.
     *
     * @throws TaskPrepareException when error resolution a some object using IOC.
     */
    @Override
    public void prepare(@NotNull IObject createCollectionMessage) throws TaskPrepareException {

        try {
            QueryStatementFactory factory = () -> {
                try {
                    CreateCollectionQuery message = IOC.resolve(
                            Keys.getOrAdd(CreateCollectionQuery.class.toString()),
                            createCollectionMessage);

                    return QueryStatementBuilder
                            .create()
                            .withCollection(message.getCollectionName().toString())
                            .withIndexes(message.getIndexes())
                            .build();
                } catch (BuildingException| ResolutionException | ChangeValueException | ReadValueException  e) {
                    throw new QueryStatementFactoryException("Error while initialize update query.", e);
                }
            };

            this.compiledQuery = IOC.resolve(
                    Keys.getOrAdd(CompiledQuery.class.toString()),
                    connection,
                    DBCreateCollectionTask.class.toString(),
                    factory);
        } catch (ResolutionException e) {
            throw new TaskPrepareException("Error while writing collection creation statement.",e);
        }
    }

    /**
     * Executes of the create collection query. Query should be a prepared before execute.
     *
     * @throws TaskExecutionException when error executes of the create collection query.
     */
    @Override
    public void execute() throws TaskExecutionException {
        try {
            compiledQuery.execute();
        } catch (QueryExecutionException e) {
            throw new TaskExecutionException("Collection creation query execution failed because of SQL exception.", e);
        }
    }

    /**
     * Setter for connection.
     */
    @Override
    public void setConnection(@NotNull StorageConnection connection) throws TaskSetConnectionException {
        this.connection = connection;
    }
}
