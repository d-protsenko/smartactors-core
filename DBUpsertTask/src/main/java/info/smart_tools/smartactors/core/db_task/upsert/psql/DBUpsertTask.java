package info.smart_tools.smartactors.core.db_task.upsert.psql;

import info.smart_tools.smartactors.core.db_storage.DataBaseStorage;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.core.db_storage.exceptions.StorageException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.db_storage.utils.ConnectionPool;
import info.smart_tools.smartactors.core.db_task.upsert.psql.exception.DBUpsertTaskException;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.sql_commons.JDBCCompiledQuery;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Task for upsert row to collection:
 * Executes update operation if incoming query contains id
 * Executes insert operation otherwise
 */
public class DBUpsertTask implements IDatabaseTask {

    private ConnectionPool connectionPool;
    private String collectionName;
    private DBInsertTask dbInsertTask;
    private IObject rawUpsertQuery;
    private Boolean isUpdate;
    private CompiledQuery compiledQuery;
    private QueryStatement updateQueryStatement;
    private QueryStatement insertQueryStatement;

    private IFieldName idFieldName;

    public DBUpsertTask(ConnectionPool connectionPool, String collectionName) throws DBUpsertTaskException {

        this.collectionName = collectionName;
        this.connectionPool = connectionPool;
        try {
            this.dbInsertTask = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), DBInsertTask.class.toString()), connectionPool, collectionName);
            this.updateQueryStatement = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), QueryStatement.class.toString()));
            this.insertQueryStatement = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), QueryStatement.class.toString()));
        } catch (ResolutionException e) {
            throw new DBUpsertTaskException("Error while resolving query statement.", e);
        }
        initUpdateQuery();
        //TODO:: move to DBInsertTask constructor or to the separate class
        initInsertQuery();
        this.isUpdate = false;
        try {
            this.idFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.toString()), collectionName + "ID");
        } catch (ResolutionException e) {
            throw new DBUpsertTaskException("Can't create idFieldName.", e);
        }
    }

    @Override
    public void prepare(final IObject upsertMessage) throws TaskPrepareException {

        this.rawUpsertQuery = upsertMessage;
        try {
            String id = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), String.class.toString()), upsertMessage.getValue(idFieldName));
            if (id != null) {

                isUpdate = true;
                updateQueryStatement.pushParameterSetter((statement, index) -> {
                    try {
                        statement.setLong(index++, Long.parseLong(id));
                        statement.setString(index++, upsertMessage.toString());
                    } catch (NullPointerException | NumberFormatException e) {
                        throw new QueryBuildException("Error while writing update query statement: ", e);
                    }
                    return index;
                });
                this.compiledQuery = connectionPool.getConnection().compileQuery(updateQueryStatement);
            } else {
                dbInsertTask.prepare(upsertMessage);

                //TODO:: move to DBInsertTask prepare() or to the separate class
//                insertQueryStatement.pushParameterSetter((statement, index) -> {
//
//                    statement.setString(index++, message.toString());
//                    return index;
//                });


                this.compiledQuery = dbInsertTask.getCompiledQuery();
            }
        } catch (ReadValueException | StorageException | ResolutionException e) {
            throw new TaskPrepareException("Error while writing update query statement.",e);
        }
    }

    @Override
    public void execute() throws TaskExecutionException {

        if (isUpdate) {
            try {
                DataBaseStorage.executeTransaction(connectionPool, (connection) -> {
                    try {
                        int nUpdated = ((JDBCCompiledQuery)compiledQuery).getPreparedStatement().executeUpdate();
                        if (nUpdated == 0) {
                            throw new QueryExecutionException("Update query failed: wrong count of documents is updated.");
                        }
                    } catch (Exception e) {
                        throw new StorageException("Collection creation query execution failed because of SQL exception.",e);
                    }
                });
                return;
            } catch (Exception e) {
                throw new TaskExecutionException("Transaction execution has been failed.", e);
            }
        }
        try {
            ResultSet resultSet = ((JDBCCompiledQuery)compiledQuery).getPreparedStatement().executeQuery();
            if (resultSet.first()) {
                try {
                    //TODO:: replace by field.inject()
                    rawUpsertQuery.setValue(idFieldName, resultSet.getLong("id"));
                } catch (ChangeValueException e) {
                    throw new StorageException("Could not set new id on inserted document.");
                }
            } else {
                throw new TaskExecutionException("Database returned not enough of generated ids.");
            }
        } catch (StorageException | SQLException e) {
            throw new TaskExecutionException("Insertion query execution failed because of SQL exception.",e);
        }
    }

    private void initUpdateQuery() throws DBUpsertTaskException {

        Writer writer = updateQueryStatement.getBodyWriter();
        try {
            writer.write(String.format(
                "UPDATE %s AS tab SET %s = docs.document FROM (VALUES", CollectionName.fromString(collectionName).toString(), "document"
            ));
            writer.write("(?,?::jsonb)");
            writer.write(String.format(") AS docs (id, document) WHERE tab.%s = docs.id;", "id"));
        } catch (IOException | QueryBuildException e) {
            throw new DBUpsertTaskException("Error while initialize update query.", e);
        }
    }

    private void initInsertQuery() throws DBUpsertTaskException {

        Writer writer = insertQueryStatement.getBodyWriter();
        try {
            writer.write(String.format(
                "INSERT INTO %s (%s) VALUES", CollectionName.fromString(collectionName).toString(), "document"
            ));
            writer.write("(?::jsonb)");
            writer.write(String.format(" RETURNING %s AS id;", "id"));
        } catch (IOException | QueryBuildException e) {
            throw new DBUpsertTaskException("Error while initialize insert query.", e);
        }
    }
}
