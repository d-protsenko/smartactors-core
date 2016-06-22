package info.smart_tools.smartactors.core.cached_collection;

import info.smart_tools.smartactors.core.cached_collection.exception.DeleteCacheItemException;
import info.smart_tools.smartactors.core.cached_collection.exception.GetCacheItemException;
import info.smart_tools.smartactors.core.cached_collection.exception.UpsertCacheItemException;
import info.smart_tools.smartactors.core.iobject.IObject;

import java.util.List;

public interface ICachedCollection {

    List<IObject> getItem(IObject query) throws GetCacheItemException;
    void delete(IObject query) throws DeleteCacheItemException;
    void upsert(IObject query) throws UpsertCacheItemException;
}