package info.smart_tools.smartactors.core.db_tasks.wrappers.delete;

import info.smart_tools.smartactors.core.db_tasks.wrappers.IDBTaskMessage;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Message with parameters for deletion query.
 * Should contains a collection name and a list of documents ids.
 */
public interface IDeleteByIdMessage extends IDBTaskMessage {
    /**
     * @return document's id.
     */
    Long getDocumentId() throws ReadValueException;

    /**
     *
     * @param documentId
     */
    void setDocumentId(Long documentId) throws ChangeValueException;
}
