package info.smart_tools.smartactors.core.cached_collection;

import info.smart_tools.smartactors.core.cached_collection.exception.DeleteCacheItemException;
import info.smart_tools.smartactors.core.cached_collection.exception.GetCacheItemException;
import info.smart_tools.smartactors.core.cached_collection.exception.UpsertCacheItemException;
import info.smart_tools.smartactors.core.cached_collection.wrapper.CachedCollectionParameters;
import info.smart_tools.smartactors.core.iobject.IObject;

import java.util.List;

/**
 * Stores objects in memory by some key and may operate with these objects into DB
 */
public interface ICachedCollection {

    /**
     * Reads active items by some key
     * @param query search query
     * @return list with found objects
     * @throws GetCacheItemException
     */
    List<IObject> getItems(CachedCollectionParameters query) throws GetCacheItemException;

    /**
     * Deletes object from memory and set active flag to false for object into DB
     * @param query deletion query
     * @throws DeleteCacheItemException
     */
    void delete(CachedCollectionParameters query) throws DeleteCacheItemException;

    /**
     * Add or update object in memory and DB
     * @param query upsert query
     * @throws UpsertCacheItemException
     */
    void upsert(CachedCollectionParameters query) throws UpsertCacheItemException;
}