package info.smart_tools.smartactors.core.db_task.upsert.psql;

import info.smart_tools.smartactors.core.db_storage.DataBaseStorage;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.core.db_storage.exceptions.StorageException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.PreparedQuery;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.db_storage.utils.ConnectionPool;
import info.smart_tools.smartactors.core.db_task.upsert.psql.wrapper.UpsertQuery;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.sql_commons.JDBCCompiledQuery;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

import java.io.IOException;
import java.io.Writer;

public class DBUpsertTask implements IDatabaseTask {

    private ConnectionPool connectionPool;
    private String collectionName;
    private DBInsertTask dbInsertTask;
    private UpsertQuery upsertQuery;
    private CompiledQuery compiledQuery;

//    private IField<String> field;

    public DBUpsertTask(ConnectionPool connectionPool, String collectionName) {

        this.collectionName = collectionName;
        this.connectionPool = connectionPool;
        //TODO:: replace new call by IOC.resolve
        this.dbInsertTask = new DBInsertTask(connectionPool, collectionName);
    }

    @Override
    public void prepare(final IObject upsertMessage) throws TaskPrepareException {

        try {

            UpsertQuery message = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), UpsertQuery.class.toString()), upsertMessage);
            QueryStatement preparedQuery = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), PreparedQuery.class.toString()));

            if (message.getId() != null) {

                Writer writer = preparedQuery.getBodyWriter();
                writer.write(String.format("UPDATE %s AS tab SET %s = docs.document FROM (VALUES",
                    CollectionName.fromString(message.getCollectionName()).toString(),
                    "document"));

                for (int i = message.countDocuments(); i > 0; --i) {
                    writer.write("(?,?::jsonb)"+((i==1)?"":","));
                }

                writer.write(String.format(") AS docs (id, document) WHERE tab.%s = docs.id;", "id"));

                preparedQuery.pushParameterSetter((statement, index) -> {
                    try {
                        statement.setLong(index++, Long.parseLong(message.getId()));
                        statement.setString(index++, message.toString());
                    } catch (ReadValueException | ChangeValueException | NullPointerException | NumberFormatException e) {
                        throw new QueryBuildException("Error while writing update query statement: ", e);
                    }
                    return index;
                });
                this.compiledQuery = connectionPool.getConnection().compileQuery(preparedQuery);
            } else {
                dbInsertTask.prepare(upsertMessage);
                this.compiledQuery = dbInsertTask.getCompiledQuery();
            }
            this.upsertQuery = message;

        } catch (ReadValueException | ChangeValueException | ResolutionException | StorageException | IOException e) {
            throw new TaskPrepareException("Error while writing update query statement.",e);
        }
    }

    @Override
    public void execute() throws TaskExecutionException {

        //TODO:: execute for insert
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
        } catch (Exception e) {
            throw new TaskExecutionException("Transaction execution has been failed.", e);
        }
    }
}
