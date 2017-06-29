package info.smart_tools.smartactors.create_postgres_collection_if_not_exists_feature.create_collection_actor.wrapper;

import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

public interface CreateCollectionWrapper {
    String getCollectionName() throws ReadValueException;
    String getConnectionOptionsRegistrationName() throws ReadValueException;
}