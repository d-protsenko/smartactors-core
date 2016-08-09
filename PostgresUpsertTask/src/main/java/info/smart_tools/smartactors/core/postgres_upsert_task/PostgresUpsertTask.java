package info.smart_tools.smartactors.core.postgres_upsert_task;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
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

/**
 * The database task which is able to upsert documents into Postgres database.
 */
public class PostgresUpsertTask implements IDatabaseTask {

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

    /**
     * Creates the task
     * @param connection the database connection where to perform upserts
     */
    public PostgresUpsertTask(final IStorageConnection connection) {
        this.connection = connection;
    }

    @Override
    public void prepare(final IObject query) throws TaskPrepareException {
        try {
            UpsertMessage message = IOC.resolve(Keys.getOrAdd(UpsertMessage.class.getCanonicalName()), query);
            collection = message.getCollectionName();
            idField = IOC.resolve(
                    Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                    String.format(PostgresSchema.ID_FIELD_PATTERN, collection.toString()));
            document = message.getDocument();
        } catch (Exception e) {
            throw new TaskPrepareException(e);
        }
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
        try {
            Object id = nextId();
            QueryStatement preparedQuery = new QueryStatement();
            PostgresSchema.insert(preparedQuery, collection);           // TODO: move the preparation steps to prepare method
            document.setValue(idField, id);

            String sqlDoc = document.serialize();
            preparedQuery.pushParameterSetter((statement, index) -> {
                statement.setString(index++, sqlDoc);
                return index;
            });

            JDBCCompiledQuery compiledQuery = (JDBCCompiledQuery) connection.compileQuery(preparedQuery);
            PreparedStatement statement = compiledQuery.getPreparedStatement();
            statement.execute();
            connection.commit();
        } catch (Exception e) {
            try {
                document.deleteField(idField);
                connection.rollback();
            } catch (Exception re) {
                // ignoring rollback failure
            }
            throw new TaskExecutionException("Insert failed", e);
        }
    }

    /**
     * Retreives the next ID for the inserting document.
     * The ID is resolved from IOC using "db.collection.nextid" key.
     * @return the new ID for the document
     */
    private Object nextId() throws ResolutionException {
        return IOC.resolve(Keys.getOrAdd("db.collection.nextid"));
    }

    /**
     * Updates the document
     * @param id id of the document to be updated
     */
    private void update(final Object id) throws TaskExecutionException {
        try {
            QueryStatement preparedQuery = new QueryStatement();
            PostgresSchema.update(preparedQuery, collection);

            String sqlDoc = document.serialize();
            preparedQuery.pushParameterSetter((statement, index) -> {
                statement.setString(index++, sqlDoc);
                statement.setObject(index++, id);
                return index;
            });

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
            throw new TaskExecutionException("Update failed", e);
        }
    }

}
