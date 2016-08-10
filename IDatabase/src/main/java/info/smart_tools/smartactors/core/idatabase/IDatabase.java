package info.smart_tools.smartactors.core.idatabase;

import info.smart_tools.smartactors.core.idatabase.exception.IDatabaseException;
import info.smart_tools.smartactors.core.iobject.IObject;

import java.util.List;

public interface IDatabase {

    void createCollection(String collectionName);

    void insert(final IObject document, final String collectionName) throws IDatabaseException;

    void update(final IObject document, final String collectionName) throws IDatabaseException;

    void upsert(final IObject document, final String collectionName) throws IDatabaseException;

    IObject getById(final Object id, final String collectionName) throws IDatabaseException;

    void delete(IObject document, final String collectionName) throws IDatabaseException;

    List<IObject> select(final IObject condition, final String collectionName) throws IDatabaseException;

}
