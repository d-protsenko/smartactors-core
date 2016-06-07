package info.smart_tools.smartactors.core.db_task.upsert.psql.wrapper;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

public interface UpsertQuery {

    String  getCollectionName() throws ReadValueException, ChangeValueException;

    /**
     * Get document-object from list by index.
     * @return an document-object to be used or updated.
     */
    IObject getDocuments(int index) throws ReadValueException, ChangeValueException;

    /**
     * Get number of document-objects
     * @return number
     */
    int countDocuments() throws ReadValueException, ChangeValueException;

    //TODO:: replace this by [CollectionName]Id field
    String getId()  throws ReadValueException, ChangeValueException;
}
