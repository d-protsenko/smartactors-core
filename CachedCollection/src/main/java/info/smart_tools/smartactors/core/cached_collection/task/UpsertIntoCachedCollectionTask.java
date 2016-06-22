package info.smart_tools.smartactors.core.cached_collection.task;

import info.smart_tools.smartactors.core.cached_collection.wrapper.UpsertIntoCachedCollectionConfig;
import info.smart_tools.smartactors.core.cached_collection.wrapper.UpsertIntoCachedCollectionQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.time.LocalDateTime;

/**
 * Makes add or update operation for cached collection
 */
public class UpsertIntoCachedCollectionTask implements IDatabaseTask {

    private IDatabaseTask upsertTask;
    private String key;

    public UpsertIntoCachedCollectionTask(UpsertIntoCachedCollectionConfig config) throws InvalidArgumentException {
        try {
            this.upsertTask = config.getUpsertTask();
            this.key = config.getKey();
        } catch (ReadValueException | ChangeValueException e) {
            throw new InvalidArgumentException("Can't create UpsertIntoCachedCollectionTask.", e);
        }
    }

    /**
     * Prepares database query
     *
     * @param query query object
     * @throws TaskPrepareException if error occurs in process of query preparing
     */
    @Override
    public void prepare(final IObject query) throws TaskPrepareException {

        try {
            UpsertIntoCachedCollectionQuery message = IOC.resolve(Keys.getOrAdd(UpsertIntoCachedCollectionQuery.class.toString()), query);
            IFieldName keyFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), key);
            String keyValue = IOC.resolve(Keys.getOrAdd(String.class.toString()), query.getValue(keyFieldName));
            if (keyValue == null || keyValue.isEmpty()) {
                throw new TaskPrepareException("Key field should be present.");
            }
            if (message.getStartDateTime() == null) {
                message.setStartDateTime(LocalDateTime.now());
            }
            upsertTask.prepare(message.wrapped());
        } catch (ResolutionException e) {
            throw new TaskPrepareException("Can't resolve objects during prepare upsert into cached collection", e);
        } catch (ReadValueException | ChangeValueException e) {
            throw new TaskPrepareException("Can't prepare query for upsert into cached collection", e);
        }
    }

    @Override
    public void setConnection(final StorageConnection connection) throws TaskSetConnectionException {
        upsertTask.setConnection(connection);
    }

    /**
     * Execute the task.
     *
     * @throws TaskExecutionException if error occurs in process of task execution
     */
    @Override
    public void execute() throws TaskExecutionException {
        upsertTask.execute();
    }
}
