package info.smart_tools.smartactors.core.db_task.get_by_id.psql;

import info.smart_tools.smartactors.core.db_storage.exceptions.StorageException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_task.get_by_id.exception.DBGetByIdTaskException;
import info.smart_tools.smartactors.core.db_task.get_by_id.psql.wrapper.SearchByIdQuery;
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
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Database task for search documents by id
 */
public class DBGetByIdTask implements IDatabaseTask {
    private StorageConnection connection;
    private SearchByIdQuery message;
    private CompiledQuery compiledQuery;
    private QueryStatement searchQueryStatement;

    /**
     * Constructor for DBGetByIdTask
     * @throws DBGetByIdTaskException
     */
    public DBGetByIdTask() throws DBGetByIdTaskException {
        try {
            this.searchQueryStatement = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), QueryStatement.class.toString()));
        } catch (ResolutionException e) {
            throw new DBGetByIdTaskException("Error while resolving query statement.", e);
        }
    }

    @Override
    public void prepare(final IObject object) throws TaskPrepareException {
        try {
            this.message = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), SearchByIdQuery.class.toString()), object);
            initQuery();

            this.compiledQuery = connection.compileQuery(searchQueryStatement);
        } catch (DBGetByIdTaskException | StorageException | ResolutionException e) {
            throw new TaskPrepareException("Error while writing search query statement.", e);
        }
    }

    @Override
    public void execute() throws TaskExecutionException {
        try {
            ResultSet resultSet = ((JDBCCompiledQuery) compiledQuery).getPreparedStatement().executeQuery();
            if (resultSet.first()) {
                try {
                    message.setSearchResult((IObject) resultSet.getObject(0));
                } catch (ChangeValueException e) {
                    throw new StorageException("Could not set the document.");
                }
            } else {
                throw new TaskExecutionException("Not found document with this id.");
            }
        } catch (ReadValueException | StorageException | SQLException e) {
            throw new TaskExecutionException("Insertion query execution failed because of SQL exception.", e);
        }
    }

    @Override
    public void setConnection(final StorageConnection connection) {
        this.connection = connection;
    }

    private void initQuery() throws DBGetByIdTaskException {
        try {
        searchQueryStatement.getBodyWriter().write(String.format("SELECT * FROM %s WHERE ", message.getCollectionName()));
        searchQueryStatement.getBodyWriter().write(String.format("token = \'%s\'", message.getId()));
        } catch (IOException | ChangeValueException | ReadValueException e) {
            throw new DBGetByIdTaskException("Error while initialize search query.", e);
        }
    }
}