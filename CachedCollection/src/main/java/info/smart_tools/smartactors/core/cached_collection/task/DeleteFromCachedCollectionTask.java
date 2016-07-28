package info.smart_tools.smartactors.core.cached_collection.task;

import info.smart_tools.smartactors.core.cached_collection.exception.CreateCachedCollectionTaskException;
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
 * Deletes object from cached collection (sets isActive flag to false)
 */
public class DeleteFromCachedCollectionTask implements IDatabaseTask {

    private IDatabaseTask updateTask;
    private IStorageConnection connection;

    private IField collectionNameField;
    private IField isActiveField;

    /**
     * @param connection storage connection for executing query
     * @throws CreateCachedCollectionTaskException for create task error
     */
    public DeleteFromCachedCollectionTask(final IStorageConnection connection) throws CreateCachedCollectionTaskException {
//        this.updateTask = updateTask;
        this.connection = connection;
        try {
            this.isActiveField = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "document/isActive");
            this.collectionNameField = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "collectionName");
        } catch (ResolutionException e) {
            throw new CreateCachedCollectionTaskException("Can't create GetObjectFromCachedCollectionTask.", e);
        }
    }

    /**
     * Prepares database query
     * @param query query object
     *              <pre>
     *              {
     *                  "document" : {CACHED ITEM},
     *                  "collectionName" : "COLLECTION_NAME"
     *              }
     *              </pre>
     * The same query would be passed to the nested task's prepare method,
     * but isActive field in document would be set to false.
     * @throws TaskPrepareException if error occurs in process of query preparing
     */
    @Override
    public void prepare(final IObject query) throws TaskPrepareException {

        try {
            isActiveField.out(query, false);
            updateTask = IOC.resolve(
                Keys.getOrAdd("db.collection.upsert"),
                connection,
                collectionNameField.in(query),
                query
            );
//            this action should be made during IOC.resolve()
//            updateTask.prepare(query);
        } catch (InvalidArgumentException | ReadValueException | ChangeValueException | ResolutionException e) {
            throw new TaskPrepareException("Can't prepare query for delete from cached collection", e);
        }

    }

    /**
     * Execute the task.
     *
     * @throws TaskExecutionException if error occurs in process of task execution
     */
    @Override
    public void execute() throws TaskExecutionException {
        updateTask.execute();
    }
}
