package info.smart_tools.smartactors.core.db_tasks.wrappers.upsert;

import info.smart_tools.smartactors.core.db_tasks.wrappers.IDBTaskMessage;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * The Message wrapper for insert or insert/update document query.
 */
public interface IUpsertMessage extends IDBTaskMessage {
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
}
