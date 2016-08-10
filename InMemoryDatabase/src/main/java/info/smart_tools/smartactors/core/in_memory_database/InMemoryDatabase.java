package info.smart_tools.smartactors.core.in_memory_database;

import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.idatabase.IDataBase;
import info.smart_tools.smartactors.core.idatabase.exception.IDataBaseException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.DeleteValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.iobject.exception.SerializeException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of data base on list
 */
public class InMemoryDatabase implements IDataBase {
    private Map<String, List<DataBaseItem>> dataBase = new HashMap<>();


    @Override
    public void upsert(final IObject document, final String collectionName) throws IDataBaseException {
        DataBaseItem item = null;
        try {
            item = IOC.resolve(Keys.getOrAdd(DataBaseItem.class.getCanonicalName()), document, collectionName);
        } catch (ResolutionException e) {
            throw new IDataBaseException("Failed to create DataBaseItem", e);
        }
        if (null == item.getId()) {
            insert(item);
        } else {
            update(item);
        }
    }

    @Override
    public void createCollection(final String collectionName) {
        dataBase.put(collectionName, new LinkedList<>());
    }

    @Override
    public void insert(final IObject document, final String collectionName) throws IDataBaseException {
        synchronized (this) {
            DataBaseItem item = null;
            try {
                item = IOC.resolve(Keys.getOrAdd(DataBaseItem.class.getCanonicalName()), document, collectionName);
            } catch (ResolutionException e) {
                throw new IDataBaseException("Failed to create DataBaseItem", e);
            }
            insert(item);
        }
    }

    @Override
    public void update(final IObject document, final String collectionName) throws IDataBaseException {
        DataBaseItem item = null;
        try {
            item = IOC.resolve(Keys.getOrAdd(DataBaseItem.class.getCanonicalName()), document, collectionName);
        } catch (ResolutionException e) {
            throw new IDataBaseException("Failed to create DataBaseItem", e);
        }
        update(item);
    }

    private void update(final DataBaseItem item) throws IDataBaseException {
        if (!dataBase.containsKey(item.getCollectionName())) {
            throw new IDataBaseException("Collection with name " + item.getCollectionName() + " does not exist");
        }
        List<DataBaseItem> list = dataBase.get(item.getCollectionName());
        for (int i = 0; i < list.size(); i++) {
            DataBaseItem inBaseElem = list.get(i);
            if (inBaseElem.getId().equals(item.getId())) {
                list.remove(i);
                list.add(i, item);
            }
        }
    }

    private void insert(final DataBaseItem item) throws IDataBaseException {
        if (!dataBase.containsKey(item.getCollectionName())) {
            throw new IDataBaseException("Collection with name " + item.getCollectionName() + " does not exist");
        }
        List<DataBaseItem> list = dataBase.get(item.getCollectionName());
        try {
            item.setId(nextId(item.getCollectionName()));
        } catch (ChangeValueException | InvalidArgumentException e) {
            throw new IDataBaseException("Failed to set id to DataBaseItem", e);
        }
        list.add(item);
    }

    @Override
    public IObject getById(final Object id, final String collectionName) throws IDataBaseException {
        if (!dataBase.containsKey(collectionName)) {
            throw new IDataBaseException("Collection with name " + collectionName + " does not exist");
        }
        List<DataBaseItem> list = dataBase.get(collectionName);
        for (DataBaseItem item : list) {
            if (item.getId().equals(id)) {
                return item.getDocument();
            }
        }
        throw new IDataBaseException("There is no element with this id");
    }

    private Object nextId(final String collectionName) {
        return dataBase.get(collectionName).size() + 1;
    }

    @Override
    public List<IObject> select(final IObject condition, final String collectionName) throws IDataBaseException {
        List<DataBaseItem> list = dataBase.get(collectionName);
        IFieldName filterFieldName = null;
        IObject filter = null;
        try {
            filterFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "filter");
            filter = (IObject) condition.getValue(filterFieldName);
        } catch (ResolutionException e) {
            throw new IDataBaseException("Failed to resolve IFieldName", e);
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new IDataBaseException("Failed to get filter from select condition", e);
        }
        List<IObject> outputList = new LinkedList<>();
        for (DataBaseItem item : list) {
            if (generalConditionParser(filter, item.getDocument())) {
                outputList.add(clone(item.getDocument()));
            }
        }
        try {
            outputList = IOC.resolve(Keys.getOrAdd("PagingForDatabaseCollection"), condition, outputList);
        } catch (ResolutionException e) {
            throw new IDataBaseException("Failed to resolve \"PagingForDatabaseCollection\"", e);
        }
        try {
            outputList = IOC.resolve(Keys.getOrAdd("SortIObjects"), condition, outputList);
        } catch (ResolutionException e) {
            throw new IDataBaseException("Failed to resolve \"SortIObjects\"", e);
        }
        return outputList;
    }

    private boolean generalConditionParser(final IObject condition, final IObject document) throws IDataBaseException {
        try {
            if (condition == null) {
                return true;
            }
            return IOC.resolve(Keys.getOrAdd("ResolveDataBaseCondition"), "$general_resolver", condition, document);
        } catch (ResolutionException e) {
            throw new IDataBaseException("Failed to resolve \"ResolveDataBaseCondition\"", e);
        }
    }

    private IObject clone(final IObject iObject) throws IDataBaseException {
        try {
            String serializedIObject = iObject.serialize();
            return IOC.resolve(Keys.getOrAdd(DSObject.class.getCanonicalName()), serializedIObject);
        } catch (ResolutionException | SerializeException e) {
            throw new IDataBaseException("Failed to clone IObject", e);
        }
    }

    @Override
    public void delete(IObject document, final String collectionName) throws IDataBaseException {
        List<DataBaseItem> list = dataBase.get(collectionName);
        DataBaseItem item = null;
        try {
            item = IOC.resolve(Keys.getOrAdd(DataBaseItem.class.getCanonicalName()), document, collectionName);
        } catch (ResolutionException e) {
            throw new IDataBaseException("Failed to create DataBaseItem", e);
        }
        for (int i = 0; i < list.size(); i++) {
            DataBaseItem inDbItem = list.get(i);
            if (inDbItem.getId().equals(item.getId())) {
                list.remove(inDbItem);
                try {
                    document.deleteField(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), collectionName + "ID"));
                } catch (DeleteValueException | InvalidArgumentException e) {
                    throw new IDataBaseException("Failed to resolve IFieldName", e);
                } catch (ResolutionException e) {
                    throw new IDataBaseException("Failed to delete field from IObject", e);
                }
                return;
            }
        }
    }
}
