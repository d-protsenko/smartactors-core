package info.smart_tools.smartactors.core.db_task.create_collection.psql.wrapper;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

import java.util.Map;

public interface CreateCollectionQuery {
    CollectionName getCollectionName() throws ReadValueException, ChangeValueException;
    void setCollectionName(CollectionName collectionName);
    /**
     *  What indexes to create on collection.
     *  @return map filedName->indexType
     *
     *  Index types:
     *  ordered     - for sortable fields (numeric or strings).
     *  tags        - for search by tags (tags field should be an JSON array).
     *
     */
    Map<String, String> getIndexes() throws ReadValueException, ChangeValueException;
    void setIndexes(Map<String, String> indexes);
}
