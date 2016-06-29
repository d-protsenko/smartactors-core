package info.smart_tools.smartactors.core.db_task.delete.wrappers;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;

import java.util.List;

/**
 * Message with parameters for deletion query.
 * Should contains a collection name and a list of documents ids.
 */
public interface IDeletionQueryMessage {
    /** The name of the collection to which the query is executed. */
    CollectionName getCollectionName();
    void setCollectionName(String CollectionName);

    /** Number of documents ids. */
    int countDocumentIds();
    /** Gives document id from the list of documents ids by index.  */
    Long getDocumentIds(int index);
    void setDocumentIds(List<Long> documentIds);
}
