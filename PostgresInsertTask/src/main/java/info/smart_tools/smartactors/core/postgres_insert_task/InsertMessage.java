package info.smart_tools.smartactors.core.postgres_insert_task;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * A query message to be passed to {@link PostgresInsertTask#prepare(IObject)}.
 */
public interface InsertMessage {

    /**
     * Returns the collection name where to insert the document
     * @return the collection name
     * @throws ReadValueException if the value cannot be read from underlying IObject
     */
    CollectionName getCollectionName() throws ReadValueException;

    /**
     * Returns the document to be inserted.
     * @return the document as IObject
     * @throws ReadValueException if the value cannot be read from underlying IObject
     */
    IObject getDocument() throws ReadValueException;

}
