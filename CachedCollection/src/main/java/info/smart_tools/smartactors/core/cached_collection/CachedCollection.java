package info.smart_tools.smartactors.core.cached_collection;

import info.smart_tools.smartactors.core.cached_collection.exception.DeleteCacheItemException;
import info.smart_tools.smartactors.core.cached_collection.exception.GetCacheItemException;
import info.smart_tools.smartactors.core.cached_collection.exception.UpsertCacheItemException;
import info.smart_tools.smartactors.core.cached_collection.task.DeleteFromCachedCollectionTask;
import info.smart_tools.smartactors.core.cached_collection.task.GetObjectFromCachedCollectionTask;
import info.smart_tools.smartactors.core.cached_collection.task.UpsertIntoCachedCollectionTask;
import info.smart_tools.smartactors.core.cached_collection.wrapper.CachedCollectionConfig;
import info.smart_tools.smartactors.core.cached_collection.wrapper.GetObjectFromCachedCollectionQuery;
import info.smart_tools.smartactors.core.cached_collection.wrapper.delete.DeleteFromCachedCollectionQuery;
import info.smart_tools.smartactors.core.cached_collection.wrapper.delete.DeleteItem;
import info.smart_tools.smartactors.core.cached_collection.wrapper.upsert.UpsertIntoCachedCollectionQuery;
import info.smart_tools.smartactors.core.cached_collection.wrapper.upsert.UpsertItem;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Implementation of cached collection
 * {@link ICachedCollection}
 */
public class CachedCollection implements ICachedCollection {

    private IPool connectionPool;
    private CollectionName collectionName;
    private ConcurrentMap<String, List<IObject>> map;

    /**
     * Constructor which initializes database tasks for db operations and connection pool for them.
     * @param config wrapper for configuration object with tasks and pool.
     * @throws InvalidArgumentException
     */
    public CachedCollection(CachedCollectionConfig config) throws InvalidArgumentException {
        try {
            this.collectionName = config.getCollectionName();
            this.connectionPool = config.getConnectionPool();
            map = new ConcurrentHashMap<>();
        } catch (ReadValueException | ChangeValueException e) {
            throw new InvalidArgumentException("Can't create cached collection.", e);
        }
    }

    @Override
    public List<IObject> getItems(String key) throws GetCacheItemException {

        try {
            List<IObject> items = map.get(key);
            if (items == null || items.isEmpty()) {
                try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {
                    GetObjectFromCachedCollectionQuery getItemQuery = IOC.resolve(Keys.getOrAdd(GetObjectFromCachedCollectionQuery.class.toString()));
                    getItemQuery.setCollectionName(this.collectionName);
                    getItemQuery.setKey(key);
                    IDatabaseTask readTask = IOC.resolve(Keys.getOrAdd(GetObjectFromCachedCollectionTask.class.toString()));
                    readTask.setConnection(IOC.resolve(Keys.getOrAdd(StorageConnection.class.toString()), poolGuard.getObject()));
                    readTask.prepare(getItemQuery.wrapped());
                    readTask.execute();
                    items = getItemQuery.getSearchResult().collect(Collectors.toList());
                } catch (PoolGuardException e) {
                    throw new GetCacheItemException("Can't get connection from pool.", e);
                }
            }

            return items;
        } catch (TaskSetConnectionException e) {
            throw new GetCacheItemException("Can't set connection to read task.", e);
        } catch (TaskPrepareException e) {
            throw new GetCacheItemException("Error during preparing read task.", e);
        } catch (TaskExecutionException e) {
            throw new GetCacheItemException("Error during execution read task.", e);
        } catch (ResolutionException e) {
            throw new GetCacheItemException("Can't resolve cached object.", e);
        } catch (ReadValueException | ChangeValueException e) {
            throw new GetCacheItemException("Can't read cached object.", e);
        }
    }

    @Override
    public void delete(IObject message) throws DeleteCacheItemException {
        try {
            DeleteItem deleteItem;
            try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {
                deleteItem = IOC.resolve(Keys.getOrAdd(DeleteItem.class.toString()), message);
                //TODO:: use connection as a part of key
                IDatabaseTask deleteTask = IOC.resolve(Keys.getOrAdd(DeleteFromCachedCollectionTask.class.toString()));
                //TODO:: if deleteTask is null, create and register it into IOC
                DeleteFromCachedCollectionQuery deleteQuery = IOC.resolve(Keys.getOrAdd(DeleteFromCachedCollectionQuery.class.toString()));
                deleteQuery.setCollectionName(this.collectionName);
                deleteQuery.setDeleteItem(deleteItem);
                deleteTask.setConnection(IOC.resolve(Keys.getOrAdd(StorageConnection.class.toString()), poolGuard.getObject()));
                deleteTask.prepare(deleteQuery.wrapped());
                deleteTask.execute();
            } catch (PoolGuardException e) {
                throw new DeleteCacheItemException("Can't get connection from pool.", e);
            } catch (TaskSetConnectionException e) {
                throw new DeleteCacheItemException("Can't set connection to delete task.", e);
            } catch (TaskPrepareException e) {
                throw new DeleteCacheItemException("Error during preparing delete task.", e);
            } catch (TaskExecutionException e) {
                throw new DeleteCacheItemException("Error during execution delete task.", e);
            }

            String key = deleteItem.getKey();
            List<IObject> items = map.get(key);
            if (items != null) {
                DeleteItem item;
                for (IObject obj : items) {
                    item = IOC.resolve(Keys.getOrAdd(DeleteItem.class.toString()), obj);
                    if (item.getId().equals(deleteItem.getId())) {
                        items.remove(obj);
                        break;
                    }
                }
            }
        }  catch (ResolutionException e) {
            throw new DeleteCacheItemException("Can't resolve cached object.", e);
        } catch (ReadValueException | ChangeValueException e) {
            throw new DeleteCacheItemException("Can't delete cached object.", e);
        }
    }

    @Override
    public void upsert(IObject message) throws UpsertCacheItemException {

        try {
            UpsertItem upsertItem;
            try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {
                upsertItem = IOC.resolve(Keys.getOrAdd(UpsertItem.class.toString()), message);
                //TODO:: use connection as a part of key
                IDatabaseTask upsertTask = IOC.resolve(Keys.getOrAdd(UpsertIntoCachedCollectionTask.class.toString()));
                //TODO:: if upsertTask is null, create and register it into IOC
                Boolean isActive = upsertItem.isActive();
                upsertItem.setIsActive(true);
                UpsertIntoCachedCollectionQuery upsertQuery = IOC.resolve(Keys.getOrAdd(UpsertIntoCachedCollectionQuery.class.toString()));
                upsertQuery.setCollectionName(this.collectionName);
                upsertQuery.setUpsertItem(upsertItem);

                upsertTask.setConnection(IOC.resolve(Keys.getOrAdd(StorageConnection.class.toString()), poolGuard.getObject()));
                upsertTask.prepare(upsertQuery.wrapped());
                try {
                    upsertTask.execute();
                } catch (TaskExecutionException e) {
                    upsertItem.setIsActive(isActive);
                    throw new UpsertCacheItemException("Error during execution upsert task.", e);
                }
            } catch (PoolGuardException e) {
                throw new UpsertCacheItemException("Can't get connection from pool.", e);
            } catch (TaskSetConnectionException e) {
                throw new UpsertCacheItemException("Can't set connection to upsert task.", e);
            } catch (TaskPrepareException e) {
                throw new UpsertCacheItemException("Error during preparing upsert task.", e);
            }
            String key = upsertItem.getKey();
            List<IObject> items = map.get(key);
            if (items != null && !items.isEmpty()) {
                UpsertItem item;
                for (IObject obj : items) {
                    item = IOC.resolve(Keys.getOrAdd(UpsertItem.class.toString()), obj);
                    if (item.getId().equals(upsertItem.getId())) {
                        items.remove(obj);
                        items.add(upsertItem.wrapped());
                        break;
                    }
                }
            } else {
                map.put(key, Collections.singletonList(upsertItem.wrapped()));
            }
        } catch (ReadValueException | ChangeValueException e) {
            throw new UpsertCacheItemException("Can't add or update cached object.", e);
        } catch (ResolutionException e) {
            throw new UpsertCacheItemException("Can't resolve cached object.", e);
        }
    }
}
