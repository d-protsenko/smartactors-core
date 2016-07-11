package info.smart_tools.smartactors.core.db_tasks.wrappers.search;

import info.smart_tools.smartactors.core.db_tasks.wrappers.IDBTaskMessage;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Wrapper for SearchById message
 */
public interface ISearchByIdMessage extends IDBTaskMessage {
    /**
     * Return the id of document
     * @return String the id of document should to be found
     * @throws ReadValueException
     */
    String getDocumentId() throws ReadValueException;

    /**
     *
     * @param id
     * @throws ChangeValueException
     */
    void setDocumentId(String id) throws ChangeValueException;

    /**
     * Set the found object to message
     * @param object the found document
     * @throws ChangeValueException
     */
    void setSearchResult(IObject object) throws ChangeValueException;

    /**
     *
     * @return
     * @throws ReadValueException
     */
    IObject getSearchResult() throws ReadValueException;
}
