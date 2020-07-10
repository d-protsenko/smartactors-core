package info.smart_tools.smartactors.database_postgresql.postgres_insert_task;

import info.smart_tools.smartactors.database.database_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.database.database_storage.utils.CollectionName;
import info.smart_tools.smartactors.database.interfaces.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.database.interfaces.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.database.interfaces.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.JDBCCompiledQuery;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.QueryStatement;
import info.smart_tools.smartactors.database_postgresql.postgres_schema.PostgresSchema;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.SerializeException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * The database task which is able to insert documents into Postgres database.
 * The inserting document must not contain ID in this collection,
 * otherwise the exception is thrown.
 */
public class PostgresInsertTask implements IDatabaseTask {

    /**
     * Connection to the database.
     */
    private IStorageConnection connection;
    /**
     * Collection where the document should be inserted.
     */
    private CollectionName collection;
    /**
     * Document to be inserted.
     */
    private IObject document;
    /**
     * Name of the ID field in the document.
     */
    private IFieldName idField;
    /**
     * Query, prepared during prepare(), to be compiled during execute().
     */
    private QueryStatement preparedQuery;

    /**
     * Creates the task
     * @param connection the database connection where to perform upserts
     */
    public PostgresInsertTask(final IStorageConnection connection) {
        this.connection = connection;
    }

    @Override
    public void prepare(final IObject query) throws TaskPrepareException {
        try {
            InsertMessage message = IOC.resolve(Keys.getKeyByName(InsertMessage.class.getCanonicalName()), query);
            collection = message.getCollectionName();
            idField = IOC.resolve(
                    Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                    String.format(PostgresSchema.ID_FIELD_PATTERN, collection.toString()));
            document = message.getDocument();

            try {
                Object id = document.getValue(idField);
                if (null == id) {
                    prepareInsert();
                } else {
                    throw new TaskPrepareException("Cannot insert document with existing " + idField + " = " + id);
                }
            } catch (ReadValueException e) {
                prepareInsert();
            }
        } catch (Exception e) {
            throw new TaskPrepareException(e);
        }
    }

    /**
     * Prepares the insert query.
     */
    private void prepareInsert() throws QueryBuildException {
        preparedQuery = new QueryStatement();
        PostgresSchema.insert(preparedQuery, collection);

        preparedQuery.pushParameterSetter((statement, index) -> {
            try {
                String sqlDoc = document.serialize();
                statement.setString(index++, sqlDoc);
            } catch (SerializeException e) {
                throw new SQLException("Cannot serialize document", e);
            }
            return index;
        });
    }

    @Override
    public void execute() throws TaskExecutionException {
        try {
            Object id = nextId();
            document.setValue(idField, id);

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
            throw new TaskExecutionException("Insert to " + collection + " failed", e);
        }
    }

    /**
     * Retrieves the next ID for the inserting document.
     * The ID is resolved from IOC using "db.collection.nextid" key.
     * @return the new ID for the document
     */
    private Object nextId() throws ResolutionException {
        return IOC.resolve(Keys.getKeyByName("db.collection.nextid"));
    }

}
