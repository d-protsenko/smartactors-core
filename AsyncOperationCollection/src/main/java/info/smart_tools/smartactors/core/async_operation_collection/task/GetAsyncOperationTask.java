package info.smart_tools.smartactors.core.async_operation_collection.task;

import info.smart_tools.smartactors.core.async_operation_collection.exception.GetAsyncOperationException;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

/**
 * Task-facade for read task for async operations collection
 */
public class GetAsyncOperationTask implements IDatabaseTask {

    private IDatabaseTask getItemTask;
    private IStorageConnection connection;

    private IField tokenField;
    private IField collectionNameField;

    /**
     * Constructor
     * @param connection connection
     * @throws GetAsyncOperationException Throw when task can't be created (for example, when can't resolve some of field)
     */
    public GetAsyncOperationTask(final IStorageConnection connection) throws GetAsyncOperationException {
        this.connection = connection;

        try {
            tokenField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "token");
            collectionNameField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "collectionName");
        } catch (ResolutionException e) {
            throw new GetAsyncOperationException("Can't resolve one of fields", e);
        }
    }

    /**
     * @param query query object
     *              {
     *                  "collectionName": "name of async operation collection",
     *                  "token": "unique token of async operation for search"
     *              }
     * @throws TaskPrepareException for prepare error
     */
    @Override
    public void prepare(final IObject query) throws TaskPrepareException {
        try {
            getItemTask = IOC.resolve(
                    Keys.getOrAdd("db.collection.getbyid"),
                    connection,
                    collectionNameField.in(query),
                    tokenField.in(query)
            );
        } catch (ResolutionException e) {
            throw new TaskPrepareException("Can't create ISearchQuery from input query", e);
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new TaskPrepareException("Can't change value in one of IObjects", e);
        }
    }

    @Override
    public void execute() throws TaskExecutionException {
        getItemTask.execute();
    }
}
