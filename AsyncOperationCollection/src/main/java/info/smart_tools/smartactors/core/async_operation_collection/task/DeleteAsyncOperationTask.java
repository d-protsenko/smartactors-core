package info.smart_tools.smartactors.core.async_operation_collection.task;

import info.smart_tools.smartactors.core.async_operation_collection.exception.DeleteAsyncOperationException;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

/**
 * Task-facade for delete task for async operations collection
 */
public class DeleteAsyncOperationTask implements IDatabaseTask {

    private IStorageConnection connection;
    private IDatabaseTask deleteTask;

    private IField collectionNameField;
    private IField documentField;

    /**
     * Constructor
     * @param connection the storage connection
     * @throws Exception
     */
    public DeleteAsyncOperationTask(final IStorageConnection connection) throws Exception {
        this.connection = connection;
        try {
            collectionNameField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "collectionName");
            documentField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "document");
        } catch (Exception e) {
            throw new DeleteAsyncOperationException("Failed to create task", e);
        }
    }

    @Override
    public void prepare(final IObject query) throws TaskPrepareException {
        try {
            deleteTask = IOC.resolve(
                    Keys.getOrAdd("db.collection.delete"),
                    connection,
                    collectionNameField.in(query),
                    documentField.in(query)
                    );
        } catch (Exception e) {
            throw new TaskPrepareException("Failed to delete async operation", e);
        }
    }

    @Override
    public void execute() throws TaskExecutionException {
        deleteTask.execute();
    }
}
