package info.smart_tools.smartactors.core.cached_collection.wrapper.upsert;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

public interface UpsertIntoCachedCollectionQuery {

    void setCollectionName(CollectionName collectionName) throws ReadValueException, ChangeValueException;
    void setUpsertItem(UpsertItem deleteItem) throws ReadValueException, ChangeValueException;

    UpsertItem getUpsertItem() throws ReadValueException, ChangeValueException;

    //NOTE:: this is proto of method instead of extractWrapped() from IObjectWrapper
    IObject wrapped() throws ReadValueException, ChangeValueException;
}
