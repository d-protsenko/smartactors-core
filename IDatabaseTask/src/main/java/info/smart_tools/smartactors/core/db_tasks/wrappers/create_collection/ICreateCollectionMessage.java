package info.smart_tools.smartactors.core.db_tasks.wrappers.create_collection;

import info.smart_tools.smartactors.core.db_tasks.wrappers.IDBTaskMessage;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

import java.util.Map;

/**
 *
 */
public interface ICreateCollectionMessage extends IDBTaskMessage {
    /**
     *  What indexes to create on collection.
     *  @return map filedName->indexType
     *
     *  Index types:
     *  ordered     - for sortable fields (numeric or strings).
     *  tags        - for search by tags (tags field should be an JSON array).
     *
     */
    Map<String, String> getIndexes() throws ReadValueException;

    /**
     *
     * @param indexes
     * @throws ChangeValueException
     */
    void setIndexes(Map<String, String> indexes) throws ChangeValueException;
}
