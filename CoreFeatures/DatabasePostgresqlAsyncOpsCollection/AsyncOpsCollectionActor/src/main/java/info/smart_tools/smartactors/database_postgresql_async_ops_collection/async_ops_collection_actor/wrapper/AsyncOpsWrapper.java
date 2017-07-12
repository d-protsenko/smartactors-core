package info.smart_tools.smartactors.database_postgresql_async_ops_collection.async_ops_collection_actor.wrapper;

import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

public interface AsyncOpsWrapper {
    String getConnectionOptionsRegistrationName() throws ReadValueException;
    String getCollectionName() throws ReadValueException;
}
