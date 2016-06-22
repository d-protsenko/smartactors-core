package info.smart_tools.smartactors.core.cached_collection;

import info.smart_tools.smartactors.core.cached_collection.exception.DeleteCacheItemException;
import info.smart_tools.smartactors.core.cached_collection.exception.GetCacheItemException;
import info.smart_tools.smartactors.core.cached_collection.exception.UpsertCacheItemException;
import info.smart_tools.smartactors.core.cached_collection.wrapper.CachedCollectionConfig;
import info.smart_tools.smartactors.core.cached_collection.wrapper.CachedItem;
import info.smart_tools.smartactors.core.cached_collection.wrapper.DeleteFromCachedCollectionQuery;
import info.smart_tools.smartactors.core.cached_collection.wrapper.UpsertIntoCachedCollectionQuery;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CachedCollection implements ICachedCollection {

    private IDatabaseTask readTask;
    private IDatabaseTask upsertTask;
    private IDatabaseTask deleteTask;
    private GetItemStrategy strategy;

    private ConcurrentMap<String, List<IObject>> map;

    //TODO:: Should be connection passed to cache?
    public CachedCollection(CachedCollectionConfig config) {
        try {
            this.readTask = config.getReadTask();
            this.upsertTask = config.getUpsertTask();
            this.deleteTask = config.getDeleteTask();
            this.strategy = config.getStrategy();
            map = new ConcurrentHashMap<>();
        } catch (ReadValueException | ChangeValueException e) {
            //TODO:: throw create cache collection exception
        }
    }

    @Override
    public IObject getItem(IObject query) throws GetCacheItemException {

        try {
            //TODO:: use wrapper and real key from it, when read task would be added
//            GetItemFromCachedCollectionQuery message = IOC.resolve(Keys.getOrAdd(GetItemFromCachedCollectionQuery.class.toString()), query);
//            String key = message.getKey();
            String key = "";
            List<IObject> items = map.get(key);
            CachedItem cachedItem = IOC.resolve(Keys.getOrAdd(CachedItem.class.toString()), strategy.getItem(items));
            Boolean isActive = cachedItem.isActive().orElse(false);
            if (!isActive) {
                readTask.prepare(query);
                readTask.execute();

                cachedItem = IOC.resolve(Keys.getOrAdd(CachedItem.class.toString()), query);
            }

            return cachedItem.wrapped();
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
    public void delete(final IObject query) throws DeleteCacheItemException {
        try {
            DeleteFromCachedCollectionQuery message = IOC.resolve(Keys.getOrAdd(DeleteFromCachedCollectionQuery.class.toString()), query);
            String key = message.getKey();
            List<IObject> items = map.get(key);
            if (items != null && !items.isEmpty()) {
                CachedItem item;
                for (IObject obj : items) {
                    //TODO:: check this
                    item = IOC.resolve(Keys.getOrAdd(CachedItem.class.toString()), obj);
                    if (item.getId().equals(message.getId())) {
                        items.remove(obj);
                        break;
                    }
                }
            }
            deleteTask.prepare(query);
            deleteTask.execute();
        } catch (TaskPrepareException e) {
            throw new DeleteCacheItemException("Error during preparing delete task.", e);
        } catch (TaskExecutionException e) {
            throw new DeleteCacheItemException("Error during execution delete task.", e);
        } catch (ResolutionException e) {
            throw new DeleteCacheItemException("Can't resolve cached object.", e);
        } catch (ReadValueException | ChangeValueException e) {
            throw new DeleteCacheItemException("Can't delete cached object.", e);
        }
    }

    @Override
    public void upsert(final IObject query) throws UpsertCacheItemException {

        try {
            UpsertIntoCachedCollectionQuery message = IOC.resolve(Keys.getOrAdd(UpsertIntoCachedCollectionQuery.class.toString()), query);
            try {
                Boolean isActive = message.isActive();
                message.setIsActive(true);
                upsertTask.prepare(query);
                try {
                    upsertTask.execute();
                } catch (TaskExecutionException e) {
                    message.setIsActive(isActive);
                    //TODO:: Should we change object into memory after exception?
                }
                String key = message.getKey();
                List<IObject> items = map.get(key);
                if (items != null && !items.isEmpty()) {
                    CachedItem item;
                    for (IObject obj : items) {
                        //TODO:: check this
                        item = IOC.resolve(Keys.getOrAdd(CachedItem.class.toString()), obj);
                        if (item.getId().equals(message.getId())) {
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
            }
        } catch (ResolutionException e) {
            throw new UpsertCacheItemException("Can't resolve cached object.", e);
        } catch (TaskPrepareException e) {
            throw new UpsertCacheItemException("Error during preparing upsert task.", e);
        }
    }
}
