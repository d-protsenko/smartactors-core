package info.smart_tools.smartactors.core.in_memory_db_create_collection_task;

import info.smart_tools.smartactors.core.idatabase.IDatabase;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.in_memory_database.InMemoryDatabase;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

/**
 * Task to create a collection in the in-memory database.
 */
public class InMemoryDBCreateCollectionTask implements IDatabaseTask {

    private IFieldName collectionNameField;
    private String collectionName;

    /**
     * Creates the task.
     * @throws TaskPrepareException if cannot resolve IFieldName
     */
    public InMemoryDBCreateCollectionTask() throws TaskPrepareException {
        try {
            collectionNameField = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "collectionName");
        } catch (ResolutionException e) {
            throw new TaskPrepareException("Failed to resolve IFieldName", e);
        }
    }

    @Override
    public void prepare(final IObject query) throws TaskPrepareException {
        try {
            this.collectionName = (String) query.getValue(collectionNameField);
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new TaskPrepareException("Failed to resolve get \"collectionName\" from query", e);
        }
    }

    @Override
    public void execute() throws TaskExecutionException {
        try {
            IDatabase dataBase = IOC.resolve(Keys.getOrAdd(InMemoryDatabase.class.getCanonicalName()));
            dataBase.createCollection(collectionName);
        } catch (ResolutionException e) {
            throw new TaskExecutionException("Failed to resolve InMemoryDatabase", e);
        }
    }
}
