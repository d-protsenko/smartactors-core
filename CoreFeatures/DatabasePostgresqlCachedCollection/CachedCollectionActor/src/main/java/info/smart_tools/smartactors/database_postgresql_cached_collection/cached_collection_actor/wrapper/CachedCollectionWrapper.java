package info.smart_tools.smartactors.database_postgresql_cached_collection.cached_collection_actor.wrapper;

import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

public interface CachedCollectionWrapper {
    String getConnectionOptionsRegistrationName() throws ReadValueException;
    String getCollectionName() throws ReadValueException;
    String getKeyName() throws ReadValueException;
}
