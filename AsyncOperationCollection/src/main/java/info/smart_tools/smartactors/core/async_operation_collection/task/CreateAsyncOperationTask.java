package info.smart_tools.smartactors.core.async_operation_collection.task;

import info.smart_tools.smartactors.core.async_operation_collection.wrapper.create_item.AsyncDocument;
import info.smart_tools.smartactors.core.async_operation_collection.wrapper.create_item.CreateOperationQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.util.Collections;

/**
 * DB task for creating operations in db
 */
public class CreateAsyncOperationTask implements IDatabaseTask {

    private IDatabaseTask task;

    /**
     * Constructor
     * @param task the insert DB task
     */
    public CreateAsyncOperationTask(final IDatabaseTask task) {
        this.task = task;
    }

    @Override
    public void prepare(final IObject query) throws TaskPrepareException {
        try {
            CreateOperationQuery srcQueryObject = IOC.resolve(Keys.getOrAdd(CreateOperationQuery.class.toString()), query);
            AsyncDocument document = IOC.resolve(Keys.getOrAdd(AsyncDocument.class.toString()));
            document.setAsyncData(srcQueryObject.getSyncData());
            document.setDoneFlag(false);
            document.setToken(srcQueryObject.getToken());
            document.setExpiredTime(srcQueryObject.getExpiredTime());
            srcQueryObject.setDocuments(Collections.singletonList(document.getIObject()));
        } catch (Exception e) {
            throw null;
        }
    }

    @Override
    public void setConnection(final StorageConnection connection) throws TaskSetConnectionException {
        this.task.setConnection(connection);
    }

    @Override
    public void execute() throws TaskExecutionException {
        this.task.execute();
    }
}
