package info.smart_tools.smartactors.core.idatabase;

import info.smart_tools.smartactors.core.idatabase.exception.IDataBaseException;
import info.smart_tools.smartactors.core.iobject.IObject;

import java.util.List;

public interface IDataBase {

    void createCollection(String collectionName);

    void insert(final IObject document, final String collectionName) throws IDataBaseException;

    void update(final IObject document, final String collectionName) throws IDataBaseException;

    void upsert(final IObject document, final String collectionName) throws IDataBaseException;

    IObject getById(final Object id, final String collectionName) throws IDataBaseException;

    void delete(IObject document, final String collectionName) throws IDataBaseException;

    List<IObject> select(final IObject condition, final String collectionName) throws IDataBaseException;

}
