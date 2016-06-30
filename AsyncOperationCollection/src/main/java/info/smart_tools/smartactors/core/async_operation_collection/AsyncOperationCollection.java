package info.smart_tools.smartactors.core.async_operation_collection;

import info.smart_tools.smartactors.core.async_operation_collection.exception.CompleteAsyncOperationException;
import info.smart_tools.smartactors.core.async_operation_collection.exception.CreateAsyncOperationException;
import info.smart_tools.smartactors.core.async_operation_collection.exception.GetAsyncOperationException;
import info.smart_tools.smartactors.core.async_operation_collection.task.CreateAsyncOperationTask;
import info.smart_tools.smartactors.core.async_operation_collection.task.GetAsyncOperationTask;
import info.smart_tools.smartactors.core.async_operation_collection.task.UpdateAsyncOperationTask;
import info.smart_tools.smartactors.core.async_operation_collection.wrapper.create_item.CreateOperationQuery;
import info.smart_tools.smartactors.core.async_operation_collection.wrapper.get_item.GetAsyncOperationQuery;
import info.smart_tools.smartactors.core.async_operation_collection.wrapper.update.UpdateAsyncOperationQuery;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.pool_guard.IPoolGuard;
import info.smart_tools.smartactors.core.pool_guard.PoolGuard;
import info.smart_tools.smartactors.core.pool_guard.exception.PoolGuardException;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;

/**
 * Implementation of collection for asynchronous operations
 * TODO:: realize cache
 */
public class AsyncOperationCollection implements IAsyncOperationCollection {

    private IPool connectionPool;
    private CollectionName collectionName;

    /**
     * Constructor for implementation
     * @param connectionPool connection pool
     * @throws InvalidArgumentException if we can't create collection name
     */
    public AsyncOperationCollection(final IPool connectionPool) throws InvalidArgumentException {
        this.connectionPool = connectionPool;
        try {
            this.collectionName = CollectionName.fromString("async_operation");
        } catch (QueryBuildException e) {
            throw new InvalidArgumentException("Can't create async operations collection.", e);
        }
    }

    @Override
    public IObject getAsyncOperation(final String token) throws GetAsyncOperationException {

        try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {
            IDatabaseTask getItemTask = IOC.resolve(Keys.getOrAdd(GetAsyncOperationTask.class.toString()));
            if (getItemTask == null) {
                IDatabaseTask nestedTask = IOC.resolve(
                    Keys.getOrAdd(IDatabaseTask.class.toString()), GetAsyncOperationTask.class.toString()
                );
                if (nestedTask == null) {
                    throw new GetAsyncOperationException("Can't create nested task for getItem task.");
                }
                getItemTask = new GetAsyncOperationTask(nestedTask);
                IOC.register(Keys.getOrAdd(GetAsyncOperationTask.class.toString()), new SingletonStrategy(getItemTask));

            }
            GetAsyncOperationQuery getItemQuery = IOC.resolve(Keys.getOrAdd(GetAsyncOperationQuery.class.toString()));
            getItemQuery.setCollectionName(collectionName);
            getItemQuery.setToken(token);
            getItemTask.setConnection(IOC.resolve(Keys.getOrAdd(StorageConnection.class.toString()), poolGuard.getObject()));
            getItemTask.prepare(getItemQuery.wrapped());
            getItemTask.execute();

            return getItemQuery.getSearchResult().get(0);
        } catch (ResolutionException e) {
            throw new GetAsyncOperationException("Can't resolve object during get operation.", e);
        } catch (InvalidArgumentException | RegistrationException e) {
            throw new GetAsyncOperationException("Can't register strategy for getItem task.", e);
        } catch (PoolGuardException e) {
            throw new GetAsyncOperationException("Can't get connection from pool.", e);
        } catch (ReadValueException | ChangeValueException e) {
            throw new GetAsyncOperationException("Can't read asynchronous operation.", e);
        } catch (TaskSetConnectionException e) {
            throw new GetAsyncOperationException("Can't set connection to read task.", e);
        } catch (TaskPrepareException e) {
            throw new GetAsyncOperationException("Error during preparing read task.", e);
        } catch (TaskExecutionException e) {
            throw new GetAsyncOperationException("Error during execution read task.", e);
        }
    }

    @Override
    public void createAsyncOperation(final IObject data, final String token) throws CreateAsyncOperationException {
        try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {
            IDatabaseTask task = IOC.resolve(Keys.getOrAdd(CreateAsyncOperationTask.class.toString()));
            if (task == null) {
                IDatabaseTask nestedTask = IOC.resolve(
                        Keys.getOrAdd(IDatabaseTask.class.toString()), CreateAsyncOperationTask.class.toString()
                );
                if (nestedTask == null) {
                    throw new CreateAsyncOperationException("Can't create nested task for createtItem task.");
                }
                task = new CreateAsyncOperationTask(nestedTask);
                IOC.register(Keys.getOrAdd(GetAsyncOperationTask.class.toString()), new SingletonStrategy(task));
            }
            CreateOperationQuery query = IOC.resolve(Keys.getOrAdd(GetAsyncOperationQuery.class.toString()));
            query.setCollectionName(collectionName);
            task.setConnection((StorageConnection) poolGuard.getObject());
            task.prepare(query.getIObject());
            task.execute();
        } catch (Exception e) {
            throw new CreateAsyncOperationException("Failed to create async operation.");
        }
    }

    @Override
    public void complete(final String token) throws CompleteAsyncOperationException {

        try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {
            IDatabaseTask updateTask = IOC.resolve(Keys.getOrAdd(UpdateAsyncOperationTask.class.toString()));
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
            //TODO:: Should it be wrapped by transaction or smth else?
            IObject updateItem = getAsyncOperation(token);
            upsertQuery.setUpdateItem(updateItem);

            updateTask.setConnection(IOC.resolve(Keys.getOrAdd(StorageConnection.class.toString()), poolGuard.getObject()));
            updateTask.prepare(IOC.resolve(Keys.getOrAdd(IObject.class.toString()), upsertQuery));
            updateTask.execute();
        } catch (TaskExecutionException e) {
            throw new CompleteAsyncOperationException("Error during execution complete.", e);
        } catch (PoolGuardException e) {
            throw new CompleteAsyncOperationException("Can't get connection from pool.", e);
        } catch (TaskSetConnectionException e) {
            throw new CompleteAsyncOperationException("Can't set connection to update task.", e);
        } catch (TaskPrepareException e) {
            throw new CompleteAsyncOperationException("Error during preparing update task.", e);
        } catch (InvalidArgumentException | RegistrationException e) {
            throw new CompleteAsyncOperationException("Can't register strategy for update task.", e);
        } catch (ChangeValueException e) {
            throw new CompleteAsyncOperationException("Can't complete async operation.", e);
        } catch (ResolutionException e) {
            throw new CompleteAsyncOperationException("Can't resolve async operation object.", e);
        } catch (GetAsyncOperationException e) {
            throw new CompleteAsyncOperationException("Can't get async operation by token.", e);
        }
    }
}
