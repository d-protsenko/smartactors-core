package info.smart_tools.smartactors.core.idatabase_task;


import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.core.itask.ITask;

/**
 * Database oriented task
 * @see ITask
 */
public interface IDatabaseTask extends ITask {

    /**
     * Prepares database query
     * @param query query object
     * @throws TaskPrepareException if error occurs in process of query preparing
     */
    void prepare(IObject query) throws TaskPrepareException;

    /**
     * Sets the connection to the database
     * @param connection connection to set
     * @throws TaskSetConnectionException when something goes wrong
     */
    void setConnection(IStorageConnection connection) throws TaskSetConnectionException;
}
