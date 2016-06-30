package info.smart_tools.smartactors.core.db_task.upsert;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.db_task.upsert.psql.wrapper.IUpsertQueryMessage;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
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
    private StorageConnection connection;

    private static final String INSERT_MODE = "INSERT";
    private static final String UPDATE_MODE = "UPDATE";

    protected DBUpsertTask() {
        subTasks = new HashMap<>(2);
    }

    @Override
    public void prepare(final IObject upsertMessage) throws TaskPrepareException {
        try {
            checkConnection(connection);
            IUpsertQueryMessage queryMessage = takeMessageWrapper(upsertMessage);
            prepareDocuments(queryMessage.getCollectionName(), queryMessage.getDocument());
            prepareSubTask(currentTask, queryMessage.getCollectionName(), queryMessage.getDocument());
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
    public void setStorageConnection(final StorageConnection storageConnection)
            throws TaskSetConnectionException {
        checkConnection(storageConnection);
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

    private IUpsertQueryMessage takeMessageWrapper(final IObject message) throws QueryBuildException {
        try {
            return IOC.resolve(
                    Keys.getOrAdd(IUpsertQueryMessage.class.toString()),
                    message);
        } catch (ResolutionException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }

    private void prepareDocuments(final CollectionName collection, final IObject document)
            throws ResolutionException, ReadValueException {
        IFieldName idFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), collection + "Id");
        String id = IOC.resolve(Keys.getOrAdd(String.class.toString()), document.getValue(idFN));
        if (id == null) {
            currentTask = subTasks.get(INSERT_MODE);
        } else {
            currentTask = subTasks.get(UPDATE_MODE);
        }
    }

    private void prepareSubTask(IDatabaseTask task, CollectionName collection, IObject document)
            throws ChangeValueException, TaskPrepareException, ResolutionException {

        IUpsertQueryMessage upsertMessage = IOC.resolve(Keys.getOrAdd(IUpsertQueryMessage.class.toString()));
        upsertMessage.setCollectionName(collection);
        upsertMessage.setDocument(document);
        IObject preparedMessage = IOC.resolve(Keys.getOrAdd("ExtractWrapper"), upsertMessage);
        task.prepare(preparedMessage);
    }

    private void checkConnection(final StorageConnection connection) throws TaskSetConnectionException {
        if (connection == null) {
            throw new TaskSetConnectionException("Connection should not be a null or empty!");
        }
        if (connection.getId() == null || connection.getId().isEmpty()) {
            throw new TaskSetConnectionException("Connection should have an id!");
        }
    }
}
