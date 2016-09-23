package info.smart_tools.smartactors.core.in_memory_db_get_by_id_task;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.idatabase.IDatabase;
import info.smart_tools.smartactors.core.idatabase.exception.IDatabaseException;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.in_memory_database.InMemoryDatabase;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

/**
 * Task for {@link InMemoryDatabase} for getting document by id
 */
public class InMemoryGetByIdTask implements IDatabaseTask {

    private IFieldName collectionNameFieldName;
    private IFieldName idFieldName;
    private IFieldName callbackFieldName;

    private String collectionName;
    private Object id;

    /**
     * Callback function to call when the object is found.
     */
    private IAction<IObject> callback;

    /**
     * Creates the task.
     * @throws TaskPrepareException if cannot resolve IFieldName
     */
    public InMemoryGetByIdTask() throws TaskPrepareException {
        try {
            collectionNameFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "collectionName");
            idFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "id");
            callbackFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "callback");
        } catch (ResolutionException e) {
            throw new TaskPrepareException("Failed to resolve IFieldName", e);
        }
    }

    @Override
    public void prepare(final IObject query) throws TaskPrepareException {
        try {
            collectionName = (String) query.getValue(collectionNameFieldName);
            id = query.getValue(idFieldName);
            callback = (IAction<IObject>) query.getValue(callbackFieldName);
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new TaskPrepareException("Failed to resolve get \"collectionName\" from query", e);
        }
    }

    @Override
    public void execute() throws TaskExecutionException {
        try {
            IDatabase dataBase = IOC.resolve(Keys.getOrAdd(InMemoryDatabase.class.getCanonicalName()));
            callback.execute(dataBase.getById(id, collectionName));
        } catch (ResolutionException e) {
            throw new TaskExecutionException("Failed to resolve InMemoryDatabase", e);
        } catch (IDatabaseException e) {
            throw new TaskExecutionException("Not found: id = " + id);
        } catch (ActionExecuteException | InvalidArgumentException e) {
            throw new TaskExecutionException("Failed to execute callback", e);
        }
    }

}
