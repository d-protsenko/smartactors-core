package info.smart_tools.smartactors.core.cached_collection.wrapper.delete;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

public interface DeleteFromCachedCollectionQuery {

    void setCollectionName(CollectionName collectionName) throws ChangeValueException;
    void setDeleteItem(DeleteItem deleteItem) throws ChangeValueException;

    DeleteItem getDeleteItem() throws ReadValueException;

    //NOTE:: this is proto of method instead of extractWrapped() from IObjectWrapper
    IObject wrapped() throws ReadValueException, ChangeValueException;
}
