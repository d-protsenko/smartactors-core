package info.smart_tools.smartactors.core.cached_collection.wrapper;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ipool.IPool;

/**
 * Wrapper for configuration iobject for cached collection
 */
public interface CachedCollectionConfig {

    /**
     * @return connection pool
     * @throws ReadValueException
     * @throws ChangeValueException
     */
    IPool getConnectionPool() throws ReadValueException, ChangeValueException;

    /**
     * @return collection name
     * @throws ReadValueException
     * @throws ChangeValueException
     */
    CollectionName getCollectionName() throws ReadValueException, ChangeValueException;

    //TODO:: Should be in another wrapper?
    void setConnectionPool(IPool connectionPool) throws ReadValueException, ChangeValueException;
    void setCollectionName(CollectionName collectionName) throws ReadValueException, ChangeValueException;
}
