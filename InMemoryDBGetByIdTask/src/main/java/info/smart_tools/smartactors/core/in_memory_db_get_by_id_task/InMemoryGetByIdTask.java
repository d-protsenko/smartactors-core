package info.smart_tools.smartactors.core.in_memory_db_get_by_id_task;

import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.idatabase.IDatabase;
import info.smart_tools.smartactors.core.idatabase.exception.IDatabaseException;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.in_memory_database.InMemoryDatabase;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

/**
 * Task for {@link InMemoryDatabase} for getting document by id
 */
public class InMemoryGetByIdTask implements IDatabaseTask {

    private String collectionName;
    private Integer id;

    /**
     * Callback function to call when the object is found.
     */
    private IAction<IObject> callback;

    @Override
    public void prepare(final IObject query) throws TaskPrepareException {
        try {
            IFieldName collectionName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "collectionName");
            this.collectionName = (String) query.getValue(collectionName);
            IFieldName idFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "id");
            this.id = (Integer) query.getValue(idFieldName);
            IFieldName callbackFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "callback");
            this.callback = (IAction<IObject>) query.getValue(callbackFieldName);
        } catch (ResolutionException e) {
            throw new TaskPrepareException("Failed to resolve IFieldName", e);
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
