package info.smart_tools.smartactors.core.async_operation_collection.task;

import info.smart_tools.smartactors.core.async_operation_collection.exception.UpdateAsyncOperationException;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
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
 * Task for mark async operation as done
 */
public class UpdateAsyncOperationTask implements IDatabaseTask {

    private IStorageConnection connection;
    private IDatabaseTask upsertTask;

    private IField doneFlagField;
    private IField documentField;
    private IField collectionNameField;

    /**
     * Constructor
     * @param connection connection for executing operations
     * @throws UpdateAsyncOperationException Throw when task can't be created (for example, when can't resolve some of field)
     */
    public UpdateAsyncOperationTask(final IStorageConnection connection) throws UpdateAsyncOperationException {
        this.connection = connection;

        try {
            doneFlagField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "done");
            documentField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "document");
            collectionNameField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "collectionName");
        } catch (ResolutionException e) {
            throw new UpdateAsyncOperationException("Can't resolve one of fields", e);
        }
    }

    @Override
    public void prepare(final IObject query) throws TaskPrepareException {

        try {
            IObject document = documentField.in(query);
            doneFlagField.out(document, true);
            upsertTask = IOC.resolve(Keys.getOrAdd("db.collection.upsert"),
                    connection,
                    collectionNameField.in(query),
                    document);
        } catch (ReadValueException | ChangeValueException | ResolutionException | InvalidArgumentException e) {
            throw new TaskPrepareException("Can't prepare query for update into async operation collection", e);
        }
    }

    @Override
    public void execute() throws TaskExecutionException {
        upsertTask.execute();
    }
}
