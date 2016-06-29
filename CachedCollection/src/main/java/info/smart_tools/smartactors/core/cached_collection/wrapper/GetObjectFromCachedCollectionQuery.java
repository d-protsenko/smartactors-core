package info.smart_tools.smartactors.core.cached_collection.wrapper;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

import java.util.stream.Stream;

public interface GetObjectFromCachedCollectionQuery {
    void setPageNumber(Integer number) throws ChangeValueException;
    void setCriteria(Object criteria) throws ChangeValueException;
    void setPageSize(Integer size) throws ChangeValueException;
    String getKey() throws ChangeValueException;// TODO: write custom name for field
    void setCollectionName(CollectionName collectionName) throws ChangeValueException;
    void setKey(String key) throws ChangeValueException;
    IObject wrapped() throws ReadValueException;
    Stream<IObject> getSearchResult() throws ReadValueException;
}
