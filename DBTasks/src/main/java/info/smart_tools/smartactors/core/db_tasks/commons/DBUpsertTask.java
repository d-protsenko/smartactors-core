package info.smart_tools.smartactors.core.db_tasks.commons;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.IStorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.ICollectionName;
import info.smart_tools.smartactors.core.db_tasks.IDatabaseTask;
import info.smart_tools.smartactors.core.db_tasks.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.db_tasks.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.db_tasks.wrappers.upsert.IUpsertMessage;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public abstract class DBUpsertTask implements IDatabaseTask {
    private Map<String, IDatabaseTask> subTasks;
    private IDatabaseTask currentTask;
    private IStorageConnection connection;

    private static final String INSERT_MODE = "INSERT";
    private static final String UPDATE_MODE = "UPDATE";

    protected DBUpsertTask() {
        subTasks = new HashMap<>(2);
    }

    @Override
    public void prepare(final IObject upsertMessage) throws TaskPrepareException {
        try {
            IUpsertMessage queryMessage = takeMessageWrapper(upsertMessage);
            prepareDocuments(queryMessage.getCollection(), queryMessage.getDocument());
            prepareSubTask(currentTask, queryMessage.getCollection(), queryMessage.getDocument());
        } catch (Exception e) {
            throw new TaskPrepareException("Can't prepare upsert task because: " + e.getMessage(), e);
        }
    }

    @Override
    public void execute() throws TaskExecutionException {
        try {
            currentTask.execute();
        } catch (Exception e) {
            throw new TaskExecutionException("'Upsert task' execution has been failed because:" + e.getMessage(), e);
        }
    }

    @Override
    public void setStorageConnection(final IStorageConnection storageConnection)
            throws TaskSetConnectionException {
        connection = storageConnection;
    }

    protected DBUpsertTask setInsertTask(@Nonnull final IDatabaseTask insertTask) {
        subTasks.put(INSERT_MODE, insertTask);
        return this;
    }

    protected DBUpsertTask setUpdatetTask(@Nonnull final IDatabaseTask updatetTask) {
        subTasks.put(UPDATE_MODE, updatetTask);
        return this;
    }

    private IUpsertMessage takeMessageWrapper(final IObject message) throws QueryBuildException {
        try {
            return IOC.resolve(
                    Keys.getOrAdd(IUpsertMessage.class.toString()),
                    message);
        } catch (ResolutionException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }

    private void prepareDocuments(final ICollectionName collection, final IObject document)
            throws ResolutionException, ReadValueException {
        IFieldName idFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), collection + "Id");
        String id = IOC.resolve(Keys.getOrAdd(String.class.toString()), document.getValue(idFN));
        if (id == null) {
            currentTask = subTasks.get(INSERT_MODE);
        } else {
            currentTask = subTasks.get(UPDATE_MODE);
        }
    }

    private void prepareSubTask(final IDatabaseTask task, final ICollectionName collection, final IObject document)
            throws ChangeValueException, TaskPrepareException, ResolutionException, TaskSetConnectionException {
        IUpsertMessage upsertMessage = IOC.resolve(Keys.getOrAdd(IUpsertMessage.class.toString()));
        upsertMessage.setCollection(collection);
        upsertMessage.setDocument(document);
        IObject preparedMessage = IOC.resolve(Keys.getOrAdd("ExtractWrapper"), upsertMessage);
        task.setStorageConnection(connection);
        task.prepare(preparedMessage);
    }
}
