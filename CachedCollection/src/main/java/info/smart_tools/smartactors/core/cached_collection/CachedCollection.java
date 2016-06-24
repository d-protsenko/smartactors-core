package info.smart_tools.smartactors.core.cached_collection;

import info.smart_tools.smartactors.core.cached_collection.exception.DeleteCacheItemException;
import info.smart_tools.smartactors.core.cached_collection.exception.GetCacheItemException;
import info.smart_tools.smartactors.core.cached_collection.exception.UpsertCacheItemException;
import info.smart_tools.smartactors.core.cached_collection.wrapper.CachedCollectionConfig;
import info.smart_tools.smartactors.core.cached_collection.wrapper.CachedCollectionParameters;
import info.smart_tools.smartactors.core.cached_collection.wrapper.CachedItem;
import info.smart_tools.smartactors.core.cached_collection.wrapper.DeleteFromCachedCollectionQuery;
import info.smart_tools.smartactors.core.cached_collection.wrapper.GetObjectFromCachedCollectionQuery;
import info.smart_tools.smartactors.core.cached_collection.wrapper.UpsertIntoCachedCollectionQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
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
    private ConcurrentMap<String, List<IObject>> map;

    /**
     * Constructor which initializes database tasks for db operations and connection pool for them.
     * @param config wrapper for configuration object with tasks and pool.
     * @throws InvalidArgumentException
     */
    public CachedCollection(CachedCollectionConfig config) throws InvalidArgumentException {
        try {
            this.connectionPool = config.getConnectionPool();
            map = new ConcurrentHashMap<>();
        } catch (ReadValueException | ChangeValueException e) {
            throw new InvalidArgumentException("Can't create cached collection.", e);
        }
    }

    @Override
    public List<IObject> getItems(IObject message) throws GetCacheItemException {

        try {
            CachedCollectionParameters params = IOC.resolve(Keys.getOrAdd(CachedCollectionParameters.class.toString()), message);
            IDatabaseTask readTask = params.getTask();
            IObject query = params.getQuery();
            GetObjectFromCachedCollectionQuery wrappedQuery = IOC.resolve(Keys.getOrAdd(GetObjectFromCachedCollectionQuery.class.toString()), query);
            String key = wrappedQuery.getKey();
            List<IObject> items = map.get(key);
            List<IObject> result = Collections.emptyList();
            if (items != null) {
                for (IObject obj : items) {
                    CachedItem cachedItem = IOC.resolve(Keys.getOrAdd(CachedItem.class.toString()), obj);
                    Boolean isActive = cachedItem.isActive().orElse(false);
                    if (isActive) {
                        result.add(cachedItem.wrapped());
                    }
                }
            }
            if (result.isEmpty()) {
                try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {
                    readTask.setConnection(IOC.resolve(Keys.getOrAdd(StorageConnection.class.toString()), poolGuard.getObject()));
                } catch (PoolGuardException e) {
                    throw new GetCacheItemException("Can't get connection from pool.", e);
                }
                readTask.prepare(query);
                readTask.execute();
                result = wrappedQuery.getSearchResult().collect(Collectors.toList());
            }
            return result;
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
            CachedCollectionParameters params = IOC.resolve(Keys.getOrAdd(CachedCollectionParameters.class.toString()), message);
            IObject query = params.getQuery();
            IDatabaseTask deleteTask = params.getTask();
            try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {
                deleteTask.setConnection(IOC.resolve(Keys.getOrAdd(StorageConnection.class.toString()), poolGuard.getObject()));
                deleteTask.prepare(query);
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

            DeleteFromCachedCollectionQuery wrappedQuery = IOC.resolve(Keys.getOrAdd(DeleteFromCachedCollectionQuery.class.toString()), query);
            String key = wrappedQuery.getKey();
            List<IObject> items = map.get(key);
            if (items != null) {
                CachedItem item;
                for (IObject obj : items) {
                    item = IOC.resolve(Keys.getOrAdd(CachedItem.class.toString()), obj);
                    if (item.getId().equals(wrappedQuery.getId())) {
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
            CachedCollectionParameters params = IOC.resolve(Keys.getOrAdd(CachedCollectionParameters.class.toString()), message);
            IObject query = params.getQuery();
            IDatabaseTask upsertTask = params.getTask();
            UpsertIntoCachedCollectionQuery wrappedQuery = IOC.resolve(Keys.getOrAdd(UpsertIntoCachedCollectionQuery.class.toString()), query);
            Boolean isActive = wrappedQuery.isActive();
            wrappedQuery.setIsActive(true);
            try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {
                upsertTask.setConnection(IOC.resolve(Keys.getOrAdd(StorageConnection.class.toString()), poolGuard.getObject()));
                upsertTask.prepare(query);
                upsertTask.execute();
            } catch (PoolGuardException e) {
                throw new UpsertCacheItemException("Can't get connection from pool.", e);
            } catch (TaskSetConnectionException e) {
                throw new UpsertCacheItemException("Can't set connection to upsert task.", e);
            } catch (TaskPrepareException e) {
                throw new UpsertCacheItemException("Error during preparing upsert task.", e);
            } catch (TaskExecutionException e) {
                wrappedQuery.setIsActive(isActive);
                throw new UpsertCacheItemException("Error during execution upsert task.", e);
            }
            String key = wrappedQuery.getKey();
            List<IObject> items = map.get(key);
            if (items != null && !items.isEmpty()) {
                CachedItem item;
                for (IObject obj : items) {
                    item = IOC.resolve(Keys.getOrAdd(CachedItem.class.toString()), obj);
                    if (item.getId().equals(wrappedQuery.getId())) {
                        items.remove(obj);
                        items.add(query);
                        break;
                    }
                }
            } else {
                map.put(key, Collections.singletonList(query));
            }
        } catch (ReadValueException | ChangeValueException e) {
            throw new UpsertCacheItemException("Can't add or update cached object.", e);
        } catch (ResolutionException e) {
            throw new UpsertCacheItemException("Can't resolve cached object.", e);
        }
    }
}
