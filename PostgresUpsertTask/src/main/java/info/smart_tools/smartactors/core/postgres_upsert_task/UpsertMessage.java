package info.smart_tools.smartactors.core.postgres_upsert_task;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * A query message to be passed to {@link PostgresUpsertTask#prepare(IObject)}.
 */
public interface UpsertMessage extends IObject {

    CollectionName getCollectionName() throws ReadValueException;

    IObject getDocument() throws ReadValueException;

}
