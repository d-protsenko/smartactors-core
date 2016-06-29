package info.smart_tools.smartactors.core.async_operation_collection.wrapper;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

import java.util.List;

public interface GetAsyncOperationQuery {
    void setCollectionName(CollectionName collectionName) throws ChangeValueException;
    void setToken(String key) throws ChangeValueException;
    String getToken() throws ReadValueException;
    //TODO:: use getIObject from IObjectWrapper
    IObject wrapped() throws ReadValueException;
    List<IObject> getSearchResult() throws ReadValueException;
    void setPageNumber(Integer pageNumber) throws ChangeValueException;
    void setPageSize(Integer pageSize) throws ChangeValueException;
    void setQuery(AsyncOperationTaskQuery query) throws ChangeValueException;
}
