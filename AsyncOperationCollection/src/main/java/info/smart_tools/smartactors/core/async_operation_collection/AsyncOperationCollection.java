package info.smart_tools.smartactors.core.async_operation_collection;

import info.smart_tools.smartactors.core.async_operation_collection.exception.CompleteAsyncOperationException;
import info.smart_tools.smartactors.core.async_operation_collection.exception.CreateAsyncOperationException;
import info.smart_tools.smartactors.core.async_operation_collection.exception.DeleteAsyncOperationException;
import info.smart_tools.smartactors.core.async_operation_collection.exception.GetAsyncOperationException;
import info.smart_tools.smartactors.core.async_operation_collection.exception.UpdateAsyncOperationException;
import info.smart_tools.smartactors.core.async_operation_collection.task.CreateAsyncOperationTask;
import info.smart_tools.smartactors.core.async_operation_collection.task.DeleteAsyncOperationTask;
import info.smart_tools.smartactors.core.async_operation_collection.task.GetAsyncOperationTask;
import info.smart_tools.smartactors.core.async_operation_collection.task.UpdateAsyncOperationTask;
import info.smart_tools.smartactors.core.async_operation_collection.wrapper.create_item.CreateOperationQuery;
import info.smart_tools.smartactors.core.async_operation_collection.wrapper.delete.DeleteAsyncOperationQuery;
import info.smart_tools.smartactors.core.async_operation_collection.wrapper.get_item.GetAsyncOperationQuery;
import info.smart_tools.smartactors.core.async_operation_collection.wrapper.update.UpdateAsyncOperationQuery;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.iobject.exception.SerializeException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.pool_guard.IPoolGuard;
import info.smart_tools.smartactors.core.pool_guard.PoolGuard;
import info.smart_tools.smartactors.core.pool_guard.exception.PoolGuardException;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Implementation of collection for asynchronous operations
 * TODO:: realize cache
 */
public class AsyncOperationCollection implements IAsyncOperationCollection {

    private IPool connectionPool;
    private String collectionName;
    private IField idField;

    /**
     * Constructor for implementation
     * @param connectionPool connection pool
     * @param collectionName string with name
     * @throws InvalidArgumentException if we can't create collection name or field
     */
    public AsyncOperationCollection(final IPool connectionPool, final String collectionName) throws InvalidArgumentException {
        this.connectionPool = connectionPool;
        try {
            this.idField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "id");
            this.collectionName = collectionName;
        } catch (ResolutionException e) {
            throw new InvalidArgumentException("Can't create field", e);
        }
    }

    @Override
    public IObject getAsyncOperation(final String token) throws GetAsyncOperationException {
        try (IPoolGuard guard = new PoolGuard(connectionPool)) {
            IObject result = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

            IDatabaseTask getItemTask = IOC.resolve(
                Keys.getOrAdd("db.async_ops_collection.get"),
                guard.getObject(),
                collectionName,
                token,
                (IAction<IObject>) doc -> {
                    try {
                        result.setValue(new FieldName("result"), doc);
                    } catch (ChangeValueException e) {
                        throw new ActionExecuteException(e);
                    }
                }
            );
            getItemTask.execute();

            IObject searchResult = (IObject) result.getValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "result"));
            if (searchResult == null) {
                throw new GetAsyncOperationException("Can't find operation.");
            }

            return searchResult;
        } catch (ResolutionException e) {
            throw new GetAsyncOperationException("Can't resolve object during get operation.", e);
        } catch (InvalidArgumentException e) {
            throw new GetAsyncOperationException("Can't register strategy for getItem task.", e);
        } catch (PoolGuardException e) {
            throw new GetAsyncOperationException("Can't get connection from pool.", e);
        } catch (ReadValueException e) {
            throw new GetAsyncOperationException("Error during preparing read task.", e);
        } catch (TaskExecutionException e) {
            throw new GetAsyncOperationException("Error during execution read task.", e);
        }
    }

    @Override
    public void createAsyncOperation(final IObject data, final String token, final String expiredTime) throws CreateAsyncOperationException {
        try {
            try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {


                IDatabaseTask createItemTask = IOC.resolve(
                        Keys.getOrAdd("db.async_ops_collection.create"),
                        poolGuard.getObject(),
                        collectionName,
                        data,
                        token,
                        expiredTime
                );
                createItemTask.execute();
            } catch (Exception e) {
                throw new CreateAsyncOperationException("Failed to create async operation.");
            }
        } catch (Exception e) {
            throw new CreateAsyncOperationException("Failed to create async operation.", e);
        }
    }

    @Override
    public void delete(final String token) throws DeleteAsyncOperationException {

        try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {

            ITask task = IOC.resolve(
                    Keys.getOrAdd("db.async_ops_collection.delete"),
                    poolGuard.getObject(),
                    collectionName,
                    token
            );
            task.execute();
        } catch (TaskExecutionException e) {
            throw new DeleteAsyncOperationException("Error during execution complete.", e);
        } catch (PoolGuardException e) {
            throw new DeleteAsyncOperationException("Can't get connection from pool.", e);
        } catch (ResolutionException e) {
            throw new DeleteAsyncOperationException("Can't get Task from IOC", e);
        }
    }


    @Override
    public void complete(final IObject asyncOperation) throws CompleteAsyncOperationException {

        try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {

            ITask updateTask = IOC.resolve(Keys.getOrAdd("db.async_ops_collection.complete"),
                    poolGuard.getObject(),
                    collectionName,
                    asyncOperation
            );

            updateTask.execute();

            /*IDatabaseTask updateTask = IOC.resolve(Keys.getOrAdd(UpdateAsyncOperationTask.class.toString()));
            if (updateTask == null) {
                IDatabaseTask nestedTask = IOC.resolve(
                    Keys.getOrAdd(IDatabaseTask.class.toString()), UpdateAsyncOperationTask.class.toString()
                );
                if (nestedTask == null) {
                    throw new CompleteAsyncOperationException("Can't create nested task for update task.");
                }
                updateTask = new UpdateAsyncOperationTask(nestedTask);
                IOC.register(Keys.getOrAdd(UpdateAsyncOperationTask.class.toString()), new SingletonStrategy(updateTask));
            }
            UpdateAsyncOperationQuery upsertQuery = IOC.resolve(Keys.getOrAdd(UpdateAsyncOperationQuery.class.toString()));
            upsertQuery.setCollectionName(collectionName);
            upsertQuery.setUpdateItem(asyncOperation);

            updateTask.setConnection(IOC.resolve(Keys.getOrAdd(StorageConnection.class.toString()), poolGuard.getObject()));
            updateTask.prepare(IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()), upsertQuery));*/
        } catch (TaskExecutionException e) {
            throw new CompleteAsyncOperationException("Error during execution complete.", e);
        } catch (PoolGuardException e) {
            throw new CompleteAsyncOperationException("Can't get connection from pool.", e);
        } catch (ResolutionException e) {
            throw new CompleteAsyncOperationException("Can't resolve async operation object.", e);
        }
    }
}
