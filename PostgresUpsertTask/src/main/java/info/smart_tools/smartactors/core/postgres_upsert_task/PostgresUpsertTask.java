package info.smart_tools.smartactors.core.postgres_upsert_task;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.core.istorage_connection.exception.StorageException;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_connection.JDBCCompiledQuery;
import info.smart_tools.smartactors.core.postgres_connection.QueryStatement;
import info.smart_tools.smartactors.core.postgres_schema.PostgresSchema;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * The database task which is able to upsert documents into Postgres database.
 */
public class PostgresUpsertTask implements IDatabaseTask {

    /**
     * Pattern for the document field with the document ID.
     */
    private static final String ID_FIELD_PATTERN = "%sID";

    /**
     * Collection where the document should be upserted.
     */
    private CollectionName collection;

    /**
     * Document to be upserted.
     */
    private IObject document;

    /**
     * Name of the ID field in the document.
     */
    private IFieldName idField;

    /**
     * Connection to the database.
     */
    private IStorageConnection connection;

    @Override
    public void prepare(final IObject query) throws TaskPrepareException {
        try {
            UpsertMessage message = IOC.resolve(Keys.getOrAdd(UpsertMessage.class.getCanonicalName()), query);
            collection = message.getCollectionName();
            idField = IOC.resolve(
                    Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                    String.format(ID_FIELD_PATTERN, collection.toString()));
            document = message.getDocument();
        } catch (Exception e) {
            throw new TaskPrepareException(e);
        }
    }

    @Override
    public void setConnection(final IStorageConnection connection) throws TaskSetConnectionException {
        this.connection = connection;
    }

    @Override
    public void execute() throws TaskExecutionException {
        try {
            try {
                Object id = document.getValue(idField);
                if (null == id) {
                    insert();
                } else {
                    update(id);
                }
            } catch (ReadValueException e) {
                insert();
            }
        } catch (InvalidArgumentException e) {
            throw new TaskExecutionException(e);
        }
    }

    /**
     * Inserts the document
     */
    private void insert() throws TaskExecutionException {
        QueryStatement preparedQuery = new QueryStatement();
        try {
            PostgresSchema.insert(preparedQuery, collection);
            preparedQuery.pushParameterSetter((statement, index) -> {
                statement.setString(index++, document.toString());
                return index;
            });
            JDBCCompiledQuery compiledQuery = (JDBCCompiledQuery) connection.compileQuery(preparedQuery);
            PreparedStatement statement = compiledQuery.getPreparedStatement();
            statement.execute();
            ResultSet resultSet = statement.getResultSet();
            long id = resultSet.getLong(1);
            document.setValue(idField, id);
            connection.commit();
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (StorageException e1) {
                // ignoring rollback failure
            }
            throw new TaskExecutionException("Insert failed", e);
        }
    }

    /**
     * Updates the document
     * @param id id of the document to be updated
     */
    private void update(final Object id) throws TaskExecutionException {
        QueryStatement preparedQuery = new QueryStatement();
        try {
            PostgresSchema.update(preparedQuery, collection);
            preparedQuery.pushParameterSetter((statement, index) -> {
                statement.setLong(index++, Long.parseLong(String.valueOf(id)));     // TODO: use IoC to convert
                statement.setString(index++, document.toString());
                return index;
            });
            JDBCCompiledQuery compiledQuery = (JDBCCompiledQuery) connection.compileQuery(preparedQuery);
            PreparedStatement statement = compiledQuery.getPreparedStatement();
            statement.execute();
            connection.commit();
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (StorageException e1) {
                // ignoring rollback failure
            }
            throw new TaskExecutionException("Update failed", e);
        }
    }

}
