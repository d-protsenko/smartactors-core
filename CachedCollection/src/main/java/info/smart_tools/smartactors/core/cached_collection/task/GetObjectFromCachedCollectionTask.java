package info.smart_tools.smartactors.core.cached_collection.task;

import info.smart_tools.smartactors.core.cached_collection.exception.CreateCachedCollectionTaskException;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.wrapper_generator.Field;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Task must search objects with target task
 */
public class GetObjectFromCachedCollectionTask implements IDatabaseTask {
    private IDatabaseTask getItemTask;

    private Field<String> collectionNameField;
    private Field<Integer> pageSizeField;
    private Field<Integer> pageNumberField;
    private Field<String> keyNameField;
    private Field<String> keyValueField;
    private Field<Boolean> criteriaEqualsIsActiveField;
    private Field<String> criteriaDateToStartDateTimeField;
    //TODO:: this format should be setted for whole project?
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * @param getItemTask Target task for getting items
     * @throws CreateCachedCollectionTaskException for error during creating
     */
    public GetObjectFromCachedCollectionTask(final IDatabaseTask getItemTask) throws CreateCachedCollectionTaskException {
        this.getItemTask = getItemTask;
        try {
            this.collectionNameField = IOC.resolve(Keys.getOrAdd("Field"), "collectionName");
            this.keyNameField = IOC.resolve(Keys.getOrAdd("Field"), "keyName");
            this.keyValueField = IOC.resolve(Keys.getOrAdd("Field"), "keyValue");
            this.pageSizeField = IOC.resolve(Keys.getOrAdd("Field"), "pageSize");
            this.pageNumberField = IOC.resolve(Keys.getOrAdd("Field"), "pageNumber");
            this.criteriaEqualsIsActiveField = IOC.resolve(Keys.getOrAdd("Field"), "criteria/isActive/$eq");
            this.criteriaDateToStartDateTimeField = IOC.resolve(Keys.getOrAdd("Field"), "criteria/startDateTime/$date-to");
        } catch (ResolutionException e) {
            throw new CreateCachedCollectionTaskException("Can't create GetObjectFromCachedCollectionTask.", e);
        }
    }

    /**
     * Prepare
     * @param query query object
     *              <pre>
     *              {
     *                  "keyName" : KEY_OF_COLLECTION,
     *                  "keyValue": "VALUE_FOR_KEY",
     *                  "collectionName" : "COLLECTION _NAME"
     *              }    
     *              </pre>
     * @throws TaskPrepareException Throw when some was incorrect in preparing query
     */
    @Override
    public void prepare(final IObject query) throws TaskPrepareException {
        try {
            IObject queryForNestedTask = IOC.resolve(Keys.getOrAdd(IObject.class.toString()));
            collectionNameField.in(queryForNestedTask, collectionNameField.out(query));
            //TODO:: remove hardcode size
            pageSizeField.in(queryForNestedTask, 100);
            pageNumberField.in(queryForNestedTask, 1);

            criteriaEqualsIsActiveField.in(queryForNestedTask, true);
            criteriaDateToStartDateTimeField.in(queryForNestedTask, LocalDateTime.now().format(FORMATTER));
            String keyName = keyNameField.out(query);
            String keyValue = keyValueField.out(query);
            Field<String> criteriaEqualsKeyField = IOC.resolve(Keys.getOrAdd("Field"), keyName + "/$eq/" + keyValue);
            criteriaEqualsKeyField.out(queryForNestedTask);
            getItemTask.prepare(queryForNestedTask);
        } catch (ResolutionException e) {
            throw new TaskPrepareException("Can't create searchQuery from input query", e);
        } catch (ChangeValueException | ReadValueException e) {
            throw new TaskPrepareException("Can't change value in one of IObjects", e);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param connection New connection for this and target tasks
     * @throws TaskSetConnectionException Throw when setting connection throw this exception
     */
    @Override
    public void setConnection(final StorageConnection connection) throws TaskSetConnectionException {
        getItemTask.setConnection(connection);
    }

    /**
     * @throws TaskExecutionException Throw when target task can't execute query
     */
    @Override
    public void execute() throws TaskExecutionException {
        getItemTask.execute();
    }
}
