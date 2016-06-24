package info.smart_tools.smartactors.core.cached_collection.wrapper;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ipool.IPool;

public interface CachedCollectionConfig {

    IPool getConnectionPool() throws ReadValueException, ChangeValueException;
    CollectionName getCollectionName() throws ReadValueException, ChangeValueException;

    //TODO:: Should be in another wrapper?
    void setConnectionPool(IPool connectionPool) throws ReadValueException, ChangeValueException;
}
