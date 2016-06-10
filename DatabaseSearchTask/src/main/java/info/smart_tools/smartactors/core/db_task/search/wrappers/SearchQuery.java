package info.smart_tools.smartactors.core.db_task.search.wrappers;

import info.smart_tools.smartactors.core.iobject.IObject;

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
public interface SearchQuery {
    /**  */
    String getCollectionName();
    void setCollectionName(String collectionName);

    /**  */
    Object getCriteria();
    void setCriteria(Object criteria);

    /**  */
    Integer getPageSize();
    void setPageSize(Integer size);

    /**  */
    Integer getPageNumber();
    void setPageNumber(Integer number);

    /**  */
    IObject getSearchResult(int index);
    /**  */
    int countSearchResult();
    void setSearchResult(Iterable<IObject> documents);

    /**  */
    IObject getOrderBy(int n);
    /**  */
    int countOrderBy();
    void setOrderBy(Iterable<IObject> order);
}
