package info.smart_tools.smartactors.core.db_task.get_by_id.psql.wrapper;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Wrapper for SearchById message
 */
public interface SearchByIdQuery {
    /**
     * Return the collectionName
     * @return String the name of collection where object is stored
     * @throws ReadValueException Calling when try read value of variable
     */
    String getCollectionName() throws ReadValueException;

    /**
     * Return the id of document
     * @return String the id of document should to be found
     * @throws ReadValueException Calling when try read value of variable
     */
    String getId() throws ReadValueException;

    /**
     * Set the found object to message
     * @param object the found document
     * @throws ChangeValueException Calling when try change value of variable
     */
    void setSearchResult(IObject object) throws ChangeValueException;
}
