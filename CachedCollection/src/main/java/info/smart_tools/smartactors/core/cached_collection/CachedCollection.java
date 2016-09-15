package info.smart_tools.smartactors.core.cached_collection;

import info.smart_tools.smartactors.core.cached_collection.exception.DeleteCacheItemException;
import info.smart_tools.smartactors.core.cached_collection.exception.GetCacheItemException;
import info.smart_tools.smartactors.core.cached_collection.exception.UpsertCacheItemException;
import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.ifield.IField;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementation of cached collection {@link ICachedCollection}
 * Resolves IDatabaseTasks for each type of operations, constructs queries for task's prepare() method,
 * sets connection from pool to task and executes it.
 */
public class CachedCollection implements ICachedCollection {

    private IField collectionNameField;
    private IField keyNameField;
    private IField specificKeyNameField;
    private IField idField;
    private IField isActiveField;

    private IPool connectionPool;
    private String collectionName;
    private String keyName;
    private ConcurrentMap<String, List<IObject>> map;

    /**
     * Constructor which initializes database tasks for db operations and connection pool for them.
     * @param config configuration object with collection settings and pool. Contains
     * {
     *               "сollectionName": "name of database collection",
     *               "connectionPool": "pool with connection objects needed for database tasks",
     *               "keyName": "name of field which stores key value into document"
     * }
     * @throws InvalidArgumentException Except when actor can't be created with @config
     */
    public CachedCollection(final IObject config) throws InvalidArgumentException {
        try {
            this.map = new ConcurrentHashMap<>();
            this.collectionNameField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "collectionName");
            IField connectionPoolField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "connectionPool");
            this.keyNameField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "keyName");
            this.isActiveField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "isActive");
            this.collectionName = collectionNameField.in(config);
            this.connectionPool = connectionPoolField.in(config);
            this.keyName = keyNameField.in(config);
            this.specificKeyNameField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), keyName);
            this.idField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), String.format("%sID", collectionName));
        } catch (ResolutionException | ReadValueException e) {
            throw new InvalidArgumentException("Can't create cached collection.", e);
        }
    }

    /**
     * Looks for value by key inside cache, if cache doesn't return any result, collection
     * resolves search task and constructs query for it. Query contains:
     * {
     *     "collectionName": "current collection name",
     *     "keyName": "name of field which stores key value into document",
     *     "keyValue": "cache key value for search"
     * }
     * Found object from DB would be saved into cache
     * @param key for cache. Cache should store needed value by this string.
     * @return list with found objects
     * @throws GetCacheItemException if any errors occurred
     */
    @Override
    public List<IObject> getItems(final String key) throws GetCacheItemException {

        try {
            final List<IObject> items = map.getOrDefault(key, new ArrayList<>());
            if (items.isEmpty()) {
                try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {
                    IDatabaseTask getItemTask = IOC.resolve(
                            Keys.getOrAdd("db.cached_collection.get_item"),
                            poolGuard.getObject(),
                            collectionName,
                            keyName,
                            key,
                            (IAction<IObject[]>) foundDocs -> {
                                try {
                                    items.addAll(Arrays.asList(foundDocs));
                                } catch (Exception e) {
                                    throw new ActionExecuteException(e);
                                }
                            }
                    );

                    getItemTask.execute();
                    map.put(key, items);
                } catch (PoolGuardException e) {
                    throw new GetCacheItemException("Can't get connection from pool.", e);
                }
            }

            return items;
        } catch (TaskExecutionException e) {
            throw new GetCacheItemException("Error during execution read task.", e);
        } catch (ResolutionException e) {
            throw new GetCacheItemException("Can't resolve cached object.", e);
        }
    }

    /**
     * Deletes object from cache and set active flag to false for object into DB.
     * Query object for task contains:
     * {
     *     "collectionName": "current collection name",
     *     "document": {iobject from message parameter}
     * }
     * @param message document for delete
     * @throws DeleteCacheItemException
     */
    @Override
    public void delete(final IObject message) throws DeleteCacheItemException {
        try {
            try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {
                IDatabaseTask deleteTask = IOC.resolve(
                    Keys.getOrAdd("db.cached_collection.delete"),
                    poolGuard.getObject(),
                    collectionName,
                    message
                );
                deleteTask.execute();
            } catch (PoolGuardException e) {
                throw new DeleteCacheItemException("Can't get connection from pool.", e);
            } catch (TaskExecutionException e) {
                throw new DeleteCacheItemException("Error during execution delete task.", e);
            }

            String key = specificKeyNameField.in(message);
            List<IObject> items = map.get(key);
            if (items != null) {
                for (IObject obj : items) {
                    if (idField.in(obj).equals(idField.in(message))) {
                        items.remove(obj);
                        break;
                    }
                }
            }
        }  catch (ResolutionException e) {
            throw new DeleteCacheItemException("Can't resolve cached object.", e);
        } catch (InvalidArgumentException | ReadValueException e) {
            throw new DeleteCacheItemException("Can't delete cached object.", e);
        }
    }

    /**
     * Adds or updates object into cache and into DB.
     * Query object for task contains:
     * {
     *     "collectionName": "current collection name",
     *     "document": {iobject from message parameter}
     * }
     * @param message document for upsert
     * @throws UpsertCacheItemException
     */
    @Override
    public void upsert(final IObject message) throws UpsertCacheItemException {

        try {
            try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {
                Boolean isActive = isActiveField.in(message);
                isActiveField.out(message, true);
                IDatabaseTask upsertTask = IOC.resolve(
                    Keys.getOrAdd("db.cached_collection.upsert"),
                    poolGuard.getObject(),
                    collectionName,
                    message
                );
                try {
                    upsertTask.execute();
                } catch (TaskExecutionException e) {
                    isActiveField.out(message, isActive);
                    throw new UpsertCacheItemException("Error during execution upsert task.", e);
                }
            } catch (PoolGuardException e) {
                throw new UpsertCacheItemException("Can't get connection from pool.", e);
            } catch (InvalidArgumentException e) {
                throw new UpsertCacheItemException("Error during operate with isActive field.", e);
            }
            String key = specificKeyNameField.in(message);
            List<IObject> items = map.get(key);
            if (items != null && !items.isEmpty()) {
                for (IObject obj : items) {
                    if (idField.in(obj).equals(idField.in(message))) {
                        items.remove(obj);
                        items.add(message);
                        break;
                    }
                }
            } else {
                map.put(key, Collections.singletonList(message));
            }
        } catch (InvalidArgumentException | ReadValueException | ChangeValueException e) {
            throw new UpsertCacheItemException("Can't add or update cached object.", e);
        } catch (ResolutionException e) {
            throw new UpsertCacheItemException("Can't resolve cached object.", e);
        }
    }
}
