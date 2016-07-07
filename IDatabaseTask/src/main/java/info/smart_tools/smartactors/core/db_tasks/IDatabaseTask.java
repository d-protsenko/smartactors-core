package info.smart_tools.smartactors.core.db_tasks;


import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_tasks.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.db_tasks.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.itask.ITask;

/**
 * Database oriented task
 * @see ITask
 */
public interface IDatabaseTask extends ITask {

    /**
     * Prepare some task for execute.
     *
     * @param message - contains parameters for preparation task.
     *
     * @throws TaskPrepareException when:
     *                1. Invalid storage connection;
     *                2. Invalid parameters in the incoming message;
     *                3. Creating query for task execute error;
     *                4. IOC resolution error;
     */
    void prepare(final IObject message) throws TaskPrepareException;

    /**
     * Setter for storage connection field.
     * @param storageConnection - database connection.
     * @throws TaskSetConnectionException when the incoming connection is invalid.
     */
    void setStorageConnection(final StorageConnection storageConnection) throws TaskSetConnectionException;
}
