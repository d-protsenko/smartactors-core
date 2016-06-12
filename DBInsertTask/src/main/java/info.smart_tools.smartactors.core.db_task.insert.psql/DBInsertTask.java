package info.smart_tools.smartactors.core.db_task.insert.psql;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.core.db_storage.exceptions.StorageException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.db_task.insert.psql.wrapper.InsertMessage;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.JDBCCompiledQuery;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;
import info.smart_tools.smartactors.core.sql_commons.psql.Schema;

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

public class DBInsertTask implements IDatabaseTask {

    private CompiledQuery compiledQuery;
    private String collectionName;
    private StorageConnection connection;
    private IFieldName idFieldName;
    private QueryStatement preparedQuery;

    public DBInsertTask() {

    }

    @Override
    public void prepare(final IObject insertQuery) throws TaskPrepareException {
        try {
            InsertMessage message = IOC.resolve(Keys.getOrAdd(InsertMessage.class.getName()), insertQuery);
            this.preparedQuery = IOC.resolve(Keys.getOrAdd(QueryStatement.class.toString()));
            this.collectionName = message.getCollectionName();
            this.idFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), collectionName + "Id");

            String id = IOC.resolve(Keys.getOrAdd(String.class.toString()), insertQuery.getValue(idFieldName));

            initPreparedQuery(message);

            if (id == null){
                preparedQuery.pushParameterSetter((statement, index) -> {
                    try {
                        statement.setString(index++ , message.toString());
                    } catch (NullPointerException | NumberFormatException e) {
                        throw new QueryBuildException("Error while write insert query statement: ", e);
                    }
                    return index;
                });
            } else {
                throw new TaskPrepareException("This object already exists/was in a collection");
            }
            compiledQuery = connection.compileQuery(preparedQuery);
        } catch (ChangeValueException | ReadValueException | ResolutionException | StorageException | IOException e) {
            throw new TaskPrepareException("Can't prepare insert query");
        }
    }

    @Override
    public void setConnection(StorageConnection connection) throws TaskSetConnectionException {
        this.connection = connection;
    }

    @Override
    public void execute() throws TaskExecutionException {
        try {
            try {
                ResultSet resultSet;
                try {
                    resultSet = ((JDBCCompiledQuery) compiledQuery).getPreparedStatement().executeQuery();
                } catch (SQLTimeoutException e){
                    throw new Exception("Timeout DB access", e);
                } catch (SQLException e){
                    throw new Exception("DataBase access error ", e);
                }

                if (resultSet == null || !resultSet.first()){
                    throw new QueryExecutionException("Database returned not enough generated ids");
                }
            } catch (Exception e){
                throw new StorageException("Collection creation query execution failed because of SQL exception.",e);
            }
        } catch (Exception e) {
            throw new TaskExecutionException("Query execution has been failed", e);
        }
    }

    private void initPreparedQuery(InsertMessage message) throws ChangeValueException, ReadValueException, QueryBuildException, IOException {
        Writer writer = preparedQuery.getBodyWriter();
        writer.write(String.format("INSERT %s AS tab SET %s = docs.document FROM (VALUES",
                CollectionName.fromString(message.getCollectionName()).toString(),
                Schema.DOCUMENT_COLUMN_NAME));
        writer.write("(?::jsonb)");
        writer.write(String.format(" RETURNING %s AS id;", Schema.ID_COLUMN_NAME));

    }
}
