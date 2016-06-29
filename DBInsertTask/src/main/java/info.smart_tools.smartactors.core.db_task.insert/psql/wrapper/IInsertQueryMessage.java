package info.smart_tools.smartactors.core.db_task.insert.psql.wrapper;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

import java.util.List;

/**
 *
 */
public interface IInsertQueryMessage {

    /**
     * Get document-object
     *
     * @param index -
     *
     * @return returns document-object to be used
     *
     * @throws ReadValueException
     */
    IObject getDocuments(int index) throws ReadValueException;
    void setDocuments(List<IObject> documents) throws ChangeValueException;
    int countDocuments();

    /**
     * Get collection name
     * @return returns name of current collection
     * @throws ChangeValueException
     * @throws ReadValueException
     */
    CollectionName getCollectionName() throws ReadValueException;
    void setCollectionName(CollectionName collectionName);

}
