package info.smart_tools.smartactors.core.db_task.insert.psql.wrapper;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

public interface IInsertMessage {

    /**
     * Get document-object
     * @return returns document-object to be used
     * @throws ReadValueException
     */
    IDocument getDocument() throws ReadValueException;
    void setDocument(IDocument document) throws ChangeValueException;

    /**
     * Get collection name
     * @return returns name of current collection
     * @throws ChangeValueException
     * @throws ReadValueException
     */
    CollectionName getCollectionName() throws ChangeValueException, ReadValueException;
    void setCollectionName(CollectionName collectionName);

}
