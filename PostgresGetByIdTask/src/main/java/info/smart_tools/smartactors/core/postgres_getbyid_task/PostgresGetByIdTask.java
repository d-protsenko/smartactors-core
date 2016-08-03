package info.smart_tools.smartactors.core.postgres_getbyid_task;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_connection.JDBCCompiledQuery;
import info.smart_tools.smartactors.core.postgres_connection.QueryStatement;
import info.smart_tools.smartactors.core.postgres_schema.PostgresSchema;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * The database task which is able to select documents from Postgres database by the document ID.
 */
public class PostgresGetByIdTask implements IDatabaseTask {

    /**
     * Collection where the document should be upserted.
     */
    private CollectionName collection;

    /**
     * Connection to the database.
     */
    private IStorageConnection connection;

    /**
     * ID to search the document.
     */
    private Object id;

    /**
     * Callback function to call when the object is found.
     */
    private IAction<IObject> callback;

    /**
     * Creates the task
     * @param connection the database connection where to perform upserts
     */
    public PostgresGetByIdTask(final IStorageConnection connection) {
        this.connection = connection;
    }

    @Override
    public void prepare(final IObject query) throws TaskPrepareException {
        try {
            GetByIdMessage message = IOC.resolve(Keys.getOrAdd(GetByIdMessage.class.getCanonicalName()), query);
            collection = message.getCollectionName();
            id = message.getId();
            callback = message.getCallback();
        } catch (Exception e) {
            throw new TaskPrepareException(e);
        }
    }

    @Override
    public void execute() throws TaskExecutionException {
        try {
            QueryStatement preparedQuery = new QueryStatement();
            PostgresSchema.getById(preparedQuery, collection);
            long sqlId = Long.parseLong(String.valueOf(id));    // TODO: use IoC to convert
            preparedQuery.pushParameterSetter((statement, index) -> {
                statement.setLong(index++, sqlId);
                return index;
            });

            JDBCCompiledQuery compiledQuery = (JDBCCompiledQuery) connection.compileQuery(preparedQuery);
            PreparedStatement statement = compiledQuery.getPreparedStatement();
            statement.execute();
            ResultSet resultSet = statement.getResultSet();
            if (resultSet.next()) {
                String sqlDoc = resultSet.getString(1);
                IObject document = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()), sqlDoc);
                callback.execute(document);
                connection.commit();
            } else {
                connection.commit();
                throw new TaskExecutionException("Not found: id = " + id);
            }
        } catch (TaskExecutionException te) {
            throw te;
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (Exception e1) {
                // ignoring rollback failure
            }
            throw new TaskExecutionException("Select failed: id = " + id, e);
        }
    }

}
