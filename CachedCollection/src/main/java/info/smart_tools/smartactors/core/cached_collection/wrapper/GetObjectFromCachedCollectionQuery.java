package info.smart_tools.smartactors.core.cached_collection.wrapper;

import info.smart_tools.smartactors.core.db_task.search.wrappers.ISearchQuery;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

public interface GetObjectFromCachedCollectionQuery extends ISearchQuery {
    String getKey() throws ReadValueException, ChangeValueException;// TODO: write custom name for field
    IObject wrapped() throws ReadValueException, ChangeValueException;
}
