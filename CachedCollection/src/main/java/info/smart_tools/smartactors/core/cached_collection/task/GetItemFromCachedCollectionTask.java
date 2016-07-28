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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Task must search objects with target task
 */
public class GetItemFromCachedCollectionTask implements IDatabaseTask {

    private IDatabaseTask getItemTask;
    private IStorageConnection connection;
    private IField collectionNameField;
    private IField pageSizeField;
    private IField pageNumberField;
    private IField keyNameField;
    private IField keyValueField;
    private IField criteriaEqualsIsActiveField;
    private IField criteriaDateToStartDateTimeField;
    //TODO:: this format should be setted for whole project?
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

    /**
     * @param connection storage connection for executing query
     * @throws CreateCachedCollectionTaskException for error during creating
     */
    public GetItemFromCachedCollectionTask(final IStorageConnection connection) throws CreateCachedCollectionTaskException {
        this.connection = connection;
        try {
            this.collectionNameField = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "collectionName");
            this.keyNameField = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "keyName");
            this.keyValueField = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "keyValue");
            this.pageSizeField = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "pageSize");
            this.pageNumberField = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "pageNumber");
            this.criteriaEqualsIsActiveField = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "criteria/isActive/$eq");
            this.criteriaDateToStartDateTimeField = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "criteria/startDateTime/$date-to");
        } catch (ResolutionException e) {
            throw new CreateCachedCollectionTaskException("Can't create GetItemFromCachedCollectionTask.", e);
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
     * Query which would be passed to the nested task:
     * TODO:: change this format after finish task's refactoring
     *              <pre>
     *              {
     *                  "pageSize": 100,
     *                  "pageNumber": 1,
     *                  "collectionName" : "COLLECTION _NAME",
     *                  "criteria": {
     *                      "isActive": {"$eq": true},
     *                      "startDateTime": {"date-to": "now"},
     *                      "<keyName>": {"$eq": "<keyValue>"}
     *                  }
     *              }
     *              </pre>
     * @throws TaskPrepareException Throw when some was incorrect in preparing query
     */
    @Override
    public void prepare(final IObject query) throws TaskPrepareException {
        try {

            //TODO:: Clarify key name for search by criteria task
            getItemTask = IOC.resolve(
                Keys.getOrAdd("db.collection.search"),
                connection,
                collectionNameField.in(query),
                query
            );
            IObject queryForNestedTask = IOC.resolve(Keys.getOrAdd(IObject.class.toString()));
            collectionNameField.out(queryForNestedTask, collectionNameField.in(query));
            //TODO:: remove hardcode size
            pageSizeField.out(queryForNestedTask, 100);
            pageNumberField.out(queryForNestedTask, 1);

            criteriaEqualsIsActiveField.out(queryForNestedTask, true);
            criteriaDateToStartDateTimeField.out(queryForNestedTask, LocalDateTime.now().format(FORMATTER));
            String keyName = keyNameField.in(query);
            String keyValue = keyValueField.in(query);
            IField criteriaEqualsKeyField = IOC.resolve(Keys.getOrAdd(IField.class.toString()), keyName + "/$eq/" + keyValue);
            criteriaEqualsKeyField.in(queryForNestedTask);
        } catch (ResolutionException e) {
            throw new TaskPrepareException("Can't create searchQuery from input query", e);
        } catch (InvalidArgumentException | ChangeValueException | ReadValueException e) {
            throw new TaskPrepareException("Can't change value in one of IObjects", e);
        }
    }

    /**
     * @throws TaskExecutionException Throw when target task can't execute query
     */
    @Override
    public void execute() throws TaskExecutionException {
        getItemTask.execute();
    }
}
