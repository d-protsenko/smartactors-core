package info.smart_tools.smartactors.core.db_task.create_collection.psql.wrapper;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

import java.util.Map;

/**
 * Wrapper for create collection query iobject
 */
public interface CreateCollectionQuery {

    /**
     * @return collection name
     * @throws ReadValueException Calling when try read value of variable
     */
    String  getCollectionName() throws ReadValueException;
    /**
     *  What indexes to create on collection.
     *  @return map filedName->indexType
     *  @throws ReadValueException Calling when try read value of variable
     *
     *  Index types:
     *  ordered     - for sortable fields (numeric or strings).
     *  tags        - for search by tags (tags field should be an JSON array).
     *
     */
    Map<String, String> getIndexes() throws ReadValueException;;
}
