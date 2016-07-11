package info.smart_tools.smartactors.core.async_operation_collection.task;

import info.smart_tools.smartactors.core.async_operation_collection.exception.CreateAsyncOperationException;
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
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

/**
 * DB task for creating operations in db
 */
public class CreateAsyncOperationTask implements IDatabaseTask {

    private IDatabaseTask task;

    private IField asyncDataField;
    private IField doneFlagField;
    private IField tokenField;
    private IField expiredTimeField;
    /**
     * Constructor
     * @param task the insert DB task
     */
    public CreateAsyncOperationTask(final IDatabaseTask task) throws CreateAsyncOperationException {
        this.task = task;

        try {
            asyncDataField = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "document/asyncData");
            doneFlagField = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "document/done");
            tokenField = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "document/token");
            expiredTimeField = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "document/expiredTime");
        } catch (ResolutionException e) {
            throw new CreateAsyncOperationException("Can't resolve one of fields", e);
        }
    }

    /**
     * @param query query object
     *     {
     *          "collectionName": "name of async operations collection",
     *          "asyncData": {iobject with data of concrete operation},
     *          "token": "unique identifier of async operation",
     *          "expiredTime": "TTL of async operation"
     *     }
     * @throws TaskPrepareException
     */
    @Override
    public void prepare(final IObject query) throws TaskPrepareException {
        try {

            asyncDataField.out(query, asyncDataField.in(query));
            doneFlagField.out(query, false);
            tokenField.out(query, tokenField.in(query));
            expiredTimeField.out(query, expiredTimeField.in(query));

            task.prepare(query);
        } catch (ReadValueException | ChangeValueException | InvalidArgumentException e) {
            throw new TaskPrepareException("Can't prepare query for create async operation cause one of IField.out operation down", e);
        }
    }

    @Override
    public void setConnection(final StorageConnection connection) throws TaskSetConnectionException {
        this.task.setConnection(connection);
    }

    @Override
    public void execute() throws TaskExecutionException {
        this.task.execute();
    }
}
