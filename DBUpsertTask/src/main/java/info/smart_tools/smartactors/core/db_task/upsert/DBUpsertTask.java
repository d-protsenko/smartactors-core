package info.smart_tools.smartactors.core.db_task.upsert;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.db_task.upsert.psql.PSQLInsertTask;
import info.smart_tools.smartactors.core.db_task.upsert.psql.PSQLUpdateTask;
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
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.util.List;
import java.util.Map;

public abstract class DBUpsertTask implements IDatabaseTask {
    private Map<String, IDatabaseTask> subTasks;
    private StorageConnection connection;
    private List<IObject> insertDocuments;
    private List<IObject> updateDocuments;

    private static final String INSERT_MODE = "insert";
    private static final String UPDATE_MODE = "update";

    protected DBUpsertTask() {
        subTasks.put(INSERT_MODE, PSQLInsertTask.create());
        subTasks.put(UPDATE_MODE, PSQLUpdateTask.create());
    }

    @Override
    public void prepare(final IObject upsertMessage) throws TaskPrepareException {
        try {
            verify(connection);
            IUpsertQueryMessage upsertMessageWrapper = takeQueryMessage(upsertMessage);

        }

    }

    @Override
    public void setStorageConnection(final StorageConnection storageConnection)
            throws TaskSetConnectionException {
        verify(storageConnection);
        connection = storageConnection;
    }

    private IUpsertQueryMessage takeQueryMessage(final IObject message) throws QueryBuildException {
        try {
            return IOC.resolve(
                    Keys.getOrAdd(IUpsertQueryMessage.class.toString()),
                    message);
        } catch (ResolutionException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }

    private void prepare(final IUpsertQueryMessage upsertMessage) throws TaskPrepareException {
        try {
            String collection = upsertMessage.getCollectionName().toString();
            prepareDocuments(collection, upsertMessage.getDocuments());
            prepareSubTask(subTasks.get(INSERT_MODE), upsertMessage.getCollectionName(), insertDocuments);
            prepareSubTask(subTasks.get(UPDATE_MODE), upsertMessage.getCollectionName(), updateDocuments);
        } catch (ReadValueException | ChangeValueException | ResolutionException e) {
            throw new TaskPrepareException();
        }
    }

    private void prepareDocuments(final String collection, final List<IObject> documents)
            throws ResolutionException, ReadValueException {

        IFieldName idFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), collection + "Id");
        for (IObject document : documents) {
            String id = IOC.resolve(Keys.getOrAdd(String.class.toString()), document.getValue(idFN));
            if (id == null) {
                insertDocuments.add(document);
            } else {
                updateDocuments.add(document);
            }
        }
    }

    private void prepareSubTask(IDatabaseTask task, CollectionName collection, List<IObject> documents)
            throws ResolutionException, ChangeValueException {
        IUpsertQueryMessage upsertMessage = IOC.resolve(Keys.getOrAdd(IUpsertQueryMessage.class.toString()));
        upsertMessage.setCollectionName(collection);
        upsertMessage.setDocuments(documents);

        task.prepare();
    }

    private void verify(final StorageConnection connection) throws TaskSetConnectionException {
        if (connection == null) {
            throw new TaskSetConnectionException("Connection should not be a null or empty!");
        }
        if (connection.getId() == null || connection.getId().isEmpty()) {
            throw new TaskSetConnectionException("Connection should have an id!");
        }
    }
}
