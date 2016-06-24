package info.smart_tools.smartactors.core.cached_collection;

import info.smart_tools.smartactors.core.cached_collection.exception.DeleteCacheItemException;
import info.smart_tools.smartactors.core.cached_collection.exception.GetCacheItemException;
import info.smart_tools.smartactors.core.cached_collection.exception.UpsertCacheItemException;
import info.smart_tools.smartactors.core.iobject.IObject;

import java.util.List;

/**
 * Stores objects in memory by some key and may operate with these objects into DB
 */
public interface ICachedCollection {

    /**
     * Reads active items by some key
     * @param key for cache
     * @return list with found objects
     * @throws GetCacheItemException
     */
    List<IObject> getItems(String key) throws GetCacheItemException;

    /**
     * Deletes object from memory and set active flag to false for object into DB
     * @param query deletion query
     * @throws DeleteCacheItemException
     */
    void delete(IObject query) throws DeleteCacheItemException;

    /**
     * Add or update object in memory and DB
     * @param query upsert query
     * @throws UpsertCacheItemException
     */
    void upsert(IObject query) throws UpsertCacheItemException;
}