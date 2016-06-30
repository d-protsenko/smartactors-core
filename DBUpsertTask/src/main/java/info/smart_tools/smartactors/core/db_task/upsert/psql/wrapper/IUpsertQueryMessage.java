package info.smart_tools.smartactors.core.db_task.upsert.psql.wrapper;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

import java.util.List;

/**
 * The Message wrapper for insert or insert/update document query.
 */
public interface IUpsertQueryMessage {
    /**
     * Takes a document from the message.
     *
     * @return {@link IObject} instance of the document.
     *
     * @throws ReadValueException when the document value couldn't been read.
     */
    IObject getDocument() throws ReadValueException;

    /**
     * Adds document field to the message.
     *
     * @param documents - documents list for inserting/updating.
     *
     * @throws ChangeValueException when the documents list field couldn't been added to the message.
     */
    void setDocument(IObject documents) throws ChangeValueException;

    /**
     * Takes collection name from the message.
     *
     * @return name of current collection.
     *
     * @throws ReadValueException when the collection name field value couldn't been read.
     */
    CollectionName getCollectionName() throws ReadValueException;

    /**
     * Adds collection name field to the message.
     *
     * @param collectionName - collection name for updating.
     *
     * @throws ChangeValueException when the collection name field couldn't been added to the message.
     */
    void setCollectionName(CollectionName collectionName) throws ChangeValueException;
}
