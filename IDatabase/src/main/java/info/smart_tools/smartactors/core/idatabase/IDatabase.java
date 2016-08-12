package info.smart_tools.smartactors.core.idatabase;

import info.smart_tools.smartactors.core.idatabase.exception.IDatabaseException;
import info.smart_tools.smartactors.core.iobject.IObject;

import java.util.List;

/**
 * Interface for a general database
 */
public interface IDatabase {

    /**
     * Creates the collection
     * @param collectionName name of the collection to create
     */
    void createCollection(String collectionName);

    /**
     * Inserts the document to the collection
     * @param document document to insert
     * @param collectionName name of the collection
     * @throws IDatabaseException if insert failed
     */
    void insert(final IObject document, final String collectionName) throws IDatabaseException;

    /**
     * Updates the document in the collection
     * @param document document to update
     * @param collectionName name of the collection
     * @throws IDatabaseException if update failed
     */
    void update(final IObject document, final String collectionName) throws IDatabaseException;

    /**
     * Upserts (inserts or updates) the document in the collection
     * @param document document to upsert
     * @param collectionName name of the collection
     * @throws IDatabaseException if upsert failed
     */
    void upsert(final IObject document, final String collectionName) throws IDatabaseException;

    /**
     * Selects the document from the collection by it's ID.
     * @param id ID of the document
     * @param collectionName name of the collection
     * @return the found document
     * @throws IDatabaseException if lookup failed
     */
    IObject getById(final Object id, final String collectionName) throws IDatabaseException;

    /**
     * Deletes the document from the collection.
     * @param document document to delete
     * @param collectionName name of the collection
     * @throws IDatabaseException if deletion failed
     */
    void delete(IObject document, final String collectionName) throws IDatabaseException;

    /**
     * Selects the documents from the collection using the specified conditions
     * @param condition criteria of search
     * @param collectionName name of the collection
     * @return list of found documents
     * @throws IDatabaseException if selection failed
     */
    List<IObject> select(final IObject condition, final String collectionName) throws IDatabaseException;

}
