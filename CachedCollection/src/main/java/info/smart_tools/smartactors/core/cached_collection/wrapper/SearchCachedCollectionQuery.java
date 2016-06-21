package info.smart_tools.smartactors.core.cached_collection.wrapper;

import info.smart_tools.smartactors.core.iobject.IObject;

public interface SearchCachedCollectionQuery {
    void setKey(IObject query);
    void setStartDateTime(IObject query);
    void setIsActive(IObject query);
}
