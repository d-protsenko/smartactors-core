package info.smart_tools.smartactors.core.postgres_create_task;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.core.istorage_connection.exception.StorageException;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_connection.JDBCCompiledQuery;
import info.smart_tools.smartactors.core.postgres_connection.QueryStatement;
import info.smart_tools.smartactors.core.postgres_schema.PostgresSchema;

import java.sql.PreparedStatement;

/**
 * The database task which is create documents collection in Postgres database.
 */
public class PostgresCreateTask implements IDatabaseTask {

    /**
     * Name of the collection to create.
     */
    private CollectionName collection;

    /**
     * Connection to the database.
     */
    private IStorageConnection connection;

    /**
     *  Options to create the collection.
     */
    private IObject options;

    /**
     * Creates the task
     * @param connection the database connection
     */
    public PostgresCreateTask(final IStorageConnection connection) {
        this.connection = connection;
    }

    @Override
    public void prepare(final IObject query) throws TaskPrepareException {
        try {
            CreateCollectionMessage message = IOC.resolve(Keys.getOrAdd(CreateCollectionMessage.class.getCanonicalName()), query);
            collection = message.getCollectionName();
            options = message.getOptions();
        } catch (Exception e) {
            throw new TaskPrepareException(e);
        }
    }

    @Override
    public void execute() throws TaskExecutionException {
        try {
            QueryStatement preparedQuery = new QueryStatement();
            PostgresSchema.create(preparedQuery, collection, options);
            JDBCCompiledQuery compiledQuery = (JDBCCompiledQuery) connection.compileQuery(preparedQuery);
            PreparedStatement statement = compiledQuery.getPreparedStatement();
            statement.execute();
            connection.commit();
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (StorageException se) {
                // ignoring rollback failure
            }
            throw new TaskExecutionException("Create collection failed", e);
        }
    }

}
