package info.smart_tools.smartactors.core.db_task.get_by_id.psql;

import info.smart_tools.smartactors.core.db_storage.DataBaseStorage;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;

/**
 * Database task for search documents by id
 */
public class DBGetByIdTask implements IDatabaseTask {
    private DataBaseStorage storage;

    @Override
    public void execute() throws TaskExecutionException {

    }

    @Override
    public void prepare(final IObject iObject) throws TaskPrepareException {

    }
}
