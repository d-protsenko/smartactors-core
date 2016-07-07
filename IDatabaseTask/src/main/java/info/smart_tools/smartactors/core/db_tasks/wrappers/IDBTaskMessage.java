package info.smart_tools.smartactors.core.db_tasks.wrappers;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 *
 */
public interface IDBTaskMessage {
    /**
     * Takes collection name from the message.
     *
     * @return name of current collection.
     *
     * @throws ReadValueException when the collection name field value couldn't been read.
     */
    CollectionName getCollection() throws ReadValueException;

    /**
     * Adds collection name field to the message.
     *
     * @param collectionName - collection name for updating.
     *
     * @throws ChangeValueException when the collection name field couldn't been added to the message.
     */
    void setCollection(CollectionName collectionName) throws ChangeValueException;
}
