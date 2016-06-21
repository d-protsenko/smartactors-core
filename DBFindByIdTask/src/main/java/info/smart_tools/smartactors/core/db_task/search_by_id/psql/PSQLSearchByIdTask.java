package info.smart_tools.smartactors.core.db_task.search_by_id.psql;

import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_task.search_by_id.DBSearchByIdTask;
import info.smart_tools.smartactors.core.db_task.search_by_id.psql.wrapper.SearchByIdQuery;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.QueryStatementFactory;
import info.smart_tools.smartactors.core.sql_commons.exception.QueryStatementFactoryException;

import javax.annotation.Nonnull;

/**
 * Database task for search documents by id
 */
public class PSQLSearchByIdTask extends DBSearchByIdTask {
    private StorageConnection connection;
    private CompiledQuery compiledQuery;
    private SearchByIdQuery message;

    /**
     * Constructor for DBGetByIdTask
     */
    public PSQLSearchByIdTask() {}

    /**
     * Prepares a search by id query for execution.
     *
     * @param object {@see SearchByIdQuery} {@link SearchByIdQuery}
     *                a message with parameters for prepares search by id query.
     *
     * @throws TaskPrepareException when error resolution a some object using IOC.
     */
    @Override
    public void prepare(@Nonnull IObject object) throws TaskPrepareException {
        try {
            QueryStatementFactory factory = () -> {
                try {
                    message = IOC.resolve(Keys.getOrAdd(SearchByIdQuery.class.toString()), object);
                    return QueryStatementBuilder
                            .create()
                            .withCollection(message.getCollectionName().toString())
                            .withId(message.getId())
                            .build();
                } catch (BuildingException | ResolutionException | ReadValueException e) {
                    throw new QueryStatementFactoryException("Error while initialize update query.", e);
                }
            };

            this.compiledQuery = IOC.resolve(
                    Keys.getOrAdd(CompiledQuery.class.toString()),
                    connection,
                    factory);
        } catch (ResolutionException e) {
            throw new TaskPrepareException("Error while writing collection creation statement.", e);
        }
    }

    /**
     * Executes of the search by id query. Query should be a prepared before execute.
     *
     * @throws TaskExecutionException when error executes of the create collection query.
     */
    @Override
    public void execute() throws TaskExecutionException {
        super.execute(compiledQuery, message);
    }

    /**
     * Setter for connection.
     */
    @Override
    public void setConnection(@Nonnull StorageConnection connection) {
        this.connection = connection;
    }
}