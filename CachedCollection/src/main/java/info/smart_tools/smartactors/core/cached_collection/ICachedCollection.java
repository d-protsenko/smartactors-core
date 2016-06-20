package info.smart_tools.smartactors.core.cached_collection;

import info.smart_tools.smartactors.core.cached_collection.exception.GetCacheItemException;
import info.smart_tools.smartactors.core.iobject.IObject;

public interface ICachedCollection {

    IObject getItem() throws GetCacheItemException;
}