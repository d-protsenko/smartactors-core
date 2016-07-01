package info.smart_tools.smartactors.core.async_operation_collection.wrapper.create_item;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

import java.util.List;

public interface CreateOperationQuery {
    void setDocuments(List<IObject> documents) throws ChangeValueException;
    void setCollectionName(CollectionName collectionName) throws ChangeValueException;
    IObject getIObject() throws ReadValueException;
    IObject getSyncData() throws ReadValueException;
    String getToken() throws ReadValueException;
    String getExpiredTime() throws ReadValueException;
}
