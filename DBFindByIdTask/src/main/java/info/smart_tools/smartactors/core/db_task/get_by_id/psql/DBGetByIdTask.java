package info.smart_tools.smartactors.core.db_task.get_by_id.psql;

import info.smart_tools.smartactors.core.db_storage.DataBaseStorage;
import info.smart_tools.smartactors.core.db_storage.exceptions.StorageException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.PreparedQuery;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.db_storage.utils.ConnectionPool;
import info.smart_tools.smartactors.core.db_task.get_by_id.psql.wrapper.SearchByIdQuery;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

import java.io.IOException;
import java.io.Writer;

/**
 * Database task for search documents by id
 */
public class DBGetByIdTask implements IDatabaseTask {


    private ConnectionPool connectionPool;
    private CompiledQuery compiledQuery;

    /**
    *Constructor
    *  @pool;
    */
    public DBGetByIdTask(final ConnectionPool pool) {
        this.connectionPool = pool;
    }

    @Override
    public void prepare(final IObject searchByIdMessage) throws TaskPrepareException {
        try {

        } catch (ReadValueException | ChangeValueException | ResolutionException | StorageException | IOException e) {
            throw new TaskPrepareException("Error while searching object by id",e);
        }
    }

    @Override
    public void execute() throws TaskExecutionException {

        try {
            DataBaseStorage.executeTransaction(connectionPool, (connection) -> {
                try {
                    ((JDBCCompiledQuery)compiledQuery).getPreparedStatement().execute();
                } catch (Exception e) {
                    throw new StorageException("Search by id execution failed because of SQL exception.",e);
                }
            });
        } catch (Exception e) {
            throw new TaskExecutionException("Transaction execution has been failed.", e);
        }
    }
}
