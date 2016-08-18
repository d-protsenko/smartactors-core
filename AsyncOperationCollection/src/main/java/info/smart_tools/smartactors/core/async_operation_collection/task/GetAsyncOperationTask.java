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

import java.time.LocalDateTime;

/**
 * Task-facade for read task for async operations collection
 */
public class GetAsyncOperationTask implements IDatabaseTask {


    private IDatabaseTask getItemTask;
    private IStorageConnection connection;

    private IField collectionNameField;
    private IField callbackField;
    private IField equalsField;
    private IField filterField;
    private IField tokenField;

    /**
     * Constructor
     * @param connection connection
     * @throws GetAsyncOperationException Throw when task can't be created (for example, when can't resolve some of field)
     */
    public GetAsyncOperationTask(final IStorageConnection connection) throws GetAsyncOperationException {
        this.connection = connection;

        try {
            this.callbackField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "callback");
            this.equalsField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "$eq");
            this.filterField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "filter");
            this.tokenField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "token");
            this.collectionNameField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "collectionName");
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
            IObject queryForNestedTask  = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
            IObject filterObject = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

            String token = tokenField.in(query);

            IObject eqKeyObject = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
            equalsField.out(eqKeyObject, token);
            tokenField.out(filterObject, eqKeyObject);

            filterField.out(queryForNestedTask, filterObject);


            getItemTask = IOC.resolve(
                    Keys.getOrAdd("db.collection.search"),
                    connection,
                    collectionNameField.in(query),
                    queryForNestedTask,
                    callbackField.in(query)
            );
        } catch (ResolutionException e) {
            throw new TaskPrepareException("Can't create ISearchQuery from input query", e);
        } catch (ReadValueException | ChangeValueException | InvalidArgumentException e) {
            throw new TaskPrepareException("Can't change value in one of IObjects", e);
        }
    }

    @Override
    public void execute() throws TaskExecutionException {
        getItemTask.execute();
    }
}
