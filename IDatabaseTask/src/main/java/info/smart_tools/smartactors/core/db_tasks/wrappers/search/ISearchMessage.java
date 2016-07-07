package info.smart_tools.smartactors.core.db_tasks.wrappers.search;

import info.smart_tools.smartactors.core.db_tasks.wrappers.IDBTaskMessage;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

import java.util.List;
import java.util.Optional;

/**
 * Interface for message containing incoming search request.
 *
 * Criteria language ("criteria" field contents) rules:
 * <pre>
 *     criteria ::= criteriaGroup
 *
 *     // criteriaGroup which has no compositionType at left side is interpreted as a "$and" criteria group.
 *     criteriaGroup ::= [criteriaGroup, ...]
 *     criteriaGroup ::= {criteriaPair, ...}
 *
 *     // Works only outside a field context.
 *     // Creates a field context with field fieldName for nested criteriaGroup.
 *     criteriaPair ::= fieldName : criteriaGroup
 *
 *     // Works only inside of field context.
 *     criteriaPair ::= constraintName : constraintParams
 *
 *     // Works everywhere.
 *     criteriaPair ::= compositionType : criteriaGroup
 *
 *     constraintName ::= "$eq" | "$gt" | "$lt" | "$tagSet" | "$tagAll" | "$tagAny"
 *
 *     // "$not" actually works as NAND (because right part should always be a criteriaGroup.
 *     compositionType ::= "$or" | "$and" | "$not"
 * </pre>
 */
public interface ISearchMessage extends IDBTaskMessage {
    /**
     *
     * @param query
     */
    void setCachedQuery(ICachedQuery query);

    /**
     *
     * @return
     */
    Optional<ICachedQuery> getCachedQuery();

    /** Criteria for searching query. */
    Object getCriteria();

    /**
     *
     * @param criteria
     */
    void setCriteria(Object criteria);

    /** Number of pages in searching query. */
    Integer getPageSize();

    /**
     *
     * @param size
     */
    void setPageSize(Integer size);

    /** A Page number at which to start searching. */
    Integer getPageNumber();

    /**
     *
     * @param number
     */
    void setPageNumber(Integer number);

    /** Order of search result list. */
    List<IObject> getOrderBy();

    /**
     *
     * @param order
     */
    void setOrderBy(List<IObject> order);

    /**
     * Set the found object to message
     * @param object the found document
     * @throws ChangeValueException
     */
    void setSearchResult(List<IObject> object) throws ChangeValueException;

    /**
     *
     * @return
     * @throws ReadValueException
     */
    List<IObject> getSearchResult() throws ReadValueException;
}
