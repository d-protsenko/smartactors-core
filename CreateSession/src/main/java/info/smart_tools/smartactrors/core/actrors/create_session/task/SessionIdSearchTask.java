package info.smart_tools.smartactrors.core.actrors.create_session.task;

import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_task.search.DBSearchTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;

/**
 * Created by Vitaly on 01.07.2016.
 */
public class SessionIdSearchTask extends DBSearchTask {
    @Override
    public void prepare(IObject query) throws TaskPrepareException {

    }

    @Override
    public void setConnection(StorageConnection connection) throws TaskSetConnectionException {

    }

    @Override
    public void execute() throws TaskExecutionException {
        execute();
    }
}
