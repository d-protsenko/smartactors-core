package info.smart_tools.smartactors.core.db_task.create_collection.psql.wrapper;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

import java.util.Map;

public interface ICreateCollectionQueryMessage {
    CollectionName getCollectionName() throws ReadValueException;
    void setCollectionName(CollectionName collectionName) throws ChangeValueException;
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
    void setIndexes(Map<String, String> indexes) throws ChangeValueException;
}
