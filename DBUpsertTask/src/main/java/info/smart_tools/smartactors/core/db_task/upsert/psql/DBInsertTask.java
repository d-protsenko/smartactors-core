package info.smart_tools.smartactors.core.db_task.upsert.psql;

import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;

//TODO:: remove this stub!
public class DBInsertTask implements IDatabaseTask {

    private CompiledQuery compiledQuery;

    public DBInsertTask() {
    }

    @Override
    public void prepare(final IObject query) throws TaskPrepareException {

    }

    @Override
    public void setConnection(final StorageConnection connection) throws TaskSetConnectionException {

    }

    @Override
    public void execute() throws TaskExecutionException {

    }

    public CompiledQuery getCompiledQuery() {
        return compiledQuery;
    }
}
