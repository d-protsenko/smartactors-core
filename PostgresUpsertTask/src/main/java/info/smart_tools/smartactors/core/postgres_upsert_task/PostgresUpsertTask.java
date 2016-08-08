package info.smart_tools.smartactors.core.postgres_upsert_task;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.iobject.exception.SerializeException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.core.istorage_connection.exception.StorageException;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_connection.JDBCCompiledQuery;
import info.smart_tools.smartactors.core.postgres_connection.QueryStatement;
import info.smart_tools.smartactors.core.postgres_connection.SQLQueryParameterSetter;
import info.smart_tools.smartactors.core.postgres_schema.PostgresSchema;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
                    String.format(ID_FIELD_PATTERN, collection.toString()));
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
            setIdAndDocumentParameters(preparedQuery, id);
            JDBCCompiledQuery compiledQuery = (JDBCCompiledQuery) connection.compileQuery(preparedQuery);
            PreparedStatement statement = compiledQuery.getPreparedStatement();
            statement.execute();
            connection.commit();
        } catch (Exception e) {
            try {
                document.deleteField(idField);
                connection.rollback();
            } catch (Exception e1) {
                // ignoring rollback failure
            }
            throw new TaskExecutionException("Insert failed", e);
        }
    }

    /**
     * Retreives the next ID for the inserting document.
     * @return the new ID for the document
     */
    private Object nextId() throws QueryBuildException, StorageException, SQLException {
        QueryStatement preparedQuery = new QueryStatement();
        PostgresSchema.nextId(preparedQuery, collection);
        JDBCCompiledQuery compiledQuery = (JDBCCompiledQuery) connection.compileQuery(preparedQuery);
        PreparedStatement statement = compiledQuery.getPreparedStatement();
        statement.execute();
        ResultSet resultSet = statement.getResultSet();
        resultSet.next();
        return resultSet.getLong(1);
    }

    /**
     * Sets two parameters to the query: id and document
     * @param query query where to set parameters using {@link QueryStatement#pushParameterSetter(SQLQueryParameterSetter)}
     * @param id id of the document, is converted to long
     * @throws SerializeException if the document cannot be converted to string
     */
    private void setIdAndDocumentParameters(final QueryStatement query, final Object id)
            throws SerializeException {
        long sqlId = Long.parseLong(String.valueOf(id));    // TODO: use IoC to convert
        String sqlDoc = document.serialize();
        query.pushParameterSetter((statement, index) -> {
            statement.setLong(index++, sqlId);
            statement.setString(index++, sqlDoc);
            return index;
        });
    }

    /**
     * Updates the document
     * @param id id of the document to be updated
     */
    private void update(final Object id) throws TaskExecutionException {
        try {
            QueryStatement preparedQuery = new QueryStatement();
            PostgresSchema.update(preparedQuery, collection);
            setIdAndDocumentParameters(preparedQuery, id);
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
