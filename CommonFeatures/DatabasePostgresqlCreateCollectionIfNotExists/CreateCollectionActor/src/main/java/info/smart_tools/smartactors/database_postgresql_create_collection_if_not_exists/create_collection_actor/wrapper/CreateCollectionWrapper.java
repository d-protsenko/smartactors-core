package info.smart_tools.smartactors.database_postgresql_create_collection_if_not_exists.create_collection_actor.wrapper;

import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

public interface CreateCollectionWrapper {
    String getCollectionName() throws ReadValueException;
    String getConnectionOptionsRegistrationName() throws ReadValueException;
}