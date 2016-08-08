package info.smart_tools.smartactors.core.in_memory_database;

import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.idatabase.IDataBase;
import info.smart_tools.smartactors.core.idatabase.exception.IDataBaseException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.iobject.exception.SerializeException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of data base on list
 */
public class InMemoryDatabase implements IDataBase {

    Map<String, IConditionVerifier> verifierMap = new HashMap<>();

    public InMemoryDatabase() {

        verifierMap.put("$general", (condition, document) -> {
                    Iterator<Map.Entry<IFieldName, Object>> iterator = condition.iterator();
                    String key = null;
                    do {
                        Map.Entry<IFieldName, Object> entry = iterator.next();
                        key = entry.getKey().toString();
                        if (entry.getValue() instanceof IObject) {
                            iterator = ((IObject) entry.getValue()).iterator();
                        }
                    } while (iterator.hasNext() && !verifierMap.containsKey(key));
                    return verifierMap.get(key).verify(condition, document);
                }
        );
        verifierMap.put("$eq", (condition, document) -> {
                    IFieldName fieldName = condition.iterator().next().getKey();
                    try {
                        Object entry = document.getValue(fieldName);
                        Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$eq"));
                        return entry.equals(reference);
                    } catch (ReadValueException | InvalidArgumentException e) {
                    }
                    return false;
                }
        );
        verifierMap.put("$neq", (condition, document) -> {
                    IFieldName fieldName = condition.iterator().next().getKey();
                    try {
                        Object entry = document.getValue(fieldName);
                        Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$eq"));
                        return !entry.equals(reference);
                    } catch (ReadValueException | InvalidArgumentException e) {
                    }
                    return false;
                }
        );
        verifierMap.put("$gt", (condition, document) -> {
                    IFieldName fieldName = condition.iterator().next().getKey();
                    try {
                        Object entry = document.getValue(fieldName);
                        Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$gt"));
                        if (reference instanceof Long) {
                            Long longEntry = (Long) entry;
                            Long longReference = (Long) reference;
                            return longEntry.compareTo(longReference) == 1;
                        }
                        if (reference instanceof Double) {
                            Double doubleReference = (Double) reference;
                            Double doubleEntry = Double.parseDouble(entry.toString());
                            return doubleEntry.compareTo(doubleReference) == 1;
                        }
                        if (reference instanceof String) {
                            String doubleEntry = (String) entry;
                            String doubleReference = (String) reference;
                            return doubleEntry.compareTo(doubleReference) == 1;
                        }
                    } catch (ReadValueException | InvalidArgumentException e) {
                    }
                    return false;
                }
        );
        verifierMap.put("$lt", (condition, document) -> {
                    IFieldName fieldName = condition.iterator().next().getKey();
                    try {
                        Object entry = document.getValue(fieldName);
                        Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$lt"));
                        if (reference instanceof Long) {
                            Long longEntry = (Long) entry;
                            Long longReference = (Long) reference;
                            return longEntry.compareTo(longReference) == -1;
                        }
                        if (reference instanceof Double) {
                            Double doubleReference = (Double) reference;
                            Double doubleEntry = Double.parseDouble(entry.toString());
                            return doubleEntry.compareTo(doubleReference) == -1;
                        }
                        if (reference instanceof String) {
                            String doubleEntry = (String) entry;
                            String doubleReference = (String) reference;
                            return doubleEntry.compareTo(doubleReference) == -1;
                        }
                    } catch (ReadValueException | InvalidArgumentException e) {
                    }
                    return false;
                }
        );
        verifierMap.put("$gte", (condition, document) -> {
                    IFieldName fieldName = condition.iterator().next().getKey();
                    try {
                        Object entry = document.getValue(fieldName);
                        Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$gte"));
                        if (reference instanceof Long) {
                            Long longEntry = (Long) entry;
                            Long longReference = (Long) reference;
                            return longEntry.compareTo(longReference) == 1 || longEntry.equals(longReference);
                        }
                        if (reference instanceof Double) {
                            Double doubleReference = (Double) reference;
                            Double doubleEntry = Double.parseDouble(entry.toString());
                            return doubleEntry.compareTo(doubleReference) == 1 || doubleEntry.equals(doubleReference);
                        }
                        if (reference instanceof String) {
                            String doubleEntry = (String) entry;
                            String doubleReference = (String) reference;
                            return doubleEntry.compareTo(doubleReference) == 1 || doubleEntry.equals(doubleReference);
                        }
                    } catch (ReadValueException | InvalidArgumentException e) {
                    }
                    return false;
                }
        );
        verifierMap.put("$date-from", (condition, document) -> {
                    IFieldName fieldName = condition.iterator().next().getKey();
                    try {
                        String entry = (String) document.getValue(fieldName);
                        String reference = String.valueOf(
                                ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$date-from"))
                        );
                        return entry.compareTo(reference) == 1 || entry.equals(reference);
                    } catch (ReadValueException | InvalidArgumentException e) {
                    }
                    return false;
                }
        );
        verifierMap.put("$date-to", (condition, document) -> {
                    IFieldName fieldName = condition.iterator().next().getKey();
                    try {
                        String entry = (String) document.getValue(fieldName);
                        String reference = String.valueOf(
                                ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$date-to"))
                        );
                        return entry.compareTo(reference) == -1 || entry.equals(reference);
                    } catch (ReadValueException | InvalidArgumentException e) {
                    }
                    return false;
                }
        );
        verifierMap.put("$lte", (condition, document) -> {
                    IFieldName fieldName = condition.iterator().next().getKey();
                    try {
                        Object entry = document.getValue(fieldName);
                        Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$lte"));
                        if (reference instanceof Long) {
                            Long longEntry = (Long) entry;
                            Long longReference = (Long) reference;
                            return longEntry.compareTo(longReference) == -1 || longEntry.equals(longReference);
                        }
                        if (reference instanceof Double) {
                            Double doubleReference = (Double) reference;
                            Double doubleEntry = Double.parseDouble(entry.toString());
                            return doubleEntry.compareTo(doubleReference) == -1 || doubleEntry.equals(doubleReference);
                        }
                        if (reference instanceof String) {
                            String doubleEntry = (String) entry;
                            String doubleReference = (String) reference;
                            return doubleEntry.compareTo(doubleReference) == -1 || doubleEntry.equals(doubleReference);
                        }
                    } catch (ReadValueException | InvalidArgumentException e) {
                    }
                    return false;
                }
        );
        verifierMap.put("$in", (condition, document) -> {
                    IFieldName fieldName = condition.iterator().next().getKey();
                    try {
                        Object entry = document.getValue(fieldName);
                        List<Object> references = (List<Object>)
                                ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$in"));
                        for (Object reference : references) {
                            if (entry.equals(reference)) {
                                return true;
                            }
                        }
                    } catch (ReadValueException | InvalidArgumentException e) {
                    }
                    return false;
                }
        );


        verifierMap.put("$and", (condition, document) -> {
                    boolean result = true;
                    try {
                        List<IObject> conditions = (List<IObject>) condition.getValue(new FieldName("$and"));
                        for (IObject conditionItem : conditions) {
                            result &= verifierMap.get("$general")
                                    .verify(conditionItem, document);
                        }
                    } catch (ReadValueException | InvalidArgumentException e) {
                    }
                    return result;
                }
        );
        verifierMap.put("$or", (condition, document) -> {
                    boolean result = false;
                    try {
                        List<IObject> conditions = (List<IObject>) condition.getValue(new FieldName("$or"));
                        for (IObject conditionItem : conditions) {
                            result |= verifierMap.get("$general")
                                    .verify(conditionItem, document);
                        }
                    } catch (ReadValueException | InvalidArgumentException e) {
                    }
                    return result;
                }
        );
        verifierMap.put("$not", (condition, document) -> {
                    boolean result = true;
                    try {
                        List<IObject> conditions = (List<IObject>) condition.getValue(new FieldName("$not"));
                        for (IObject conditionItem : conditions) {
                            result &= !verifierMap.get("$general")
                                    .verify(conditionItem, document);
                        }
                    } catch (ReadValueException | InvalidArgumentException e) {
                    }
                    return result;
                }
        );
    }

    private List<DataBaseItem> list = new LinkedList<>();

    @Override
    public void upsert(final IObject document, final String collectionName) throws IDataBaseException {
        DataBaseItem item = null;
        try {
            item = new DataBaseItem(document, collectionName);
        } catch (ResolutionException | ReadValueException | InvalidArgumentException e) {
            throw new IDataBaseException("Failed to create DataBaseItem", e);
        }
        if (null == item.getId()) {
            insert(item);
        } else {
            update(item);
        }
    }

    @Override
    public void insert(final IObject document, final String collectionName) throws IDataBaseException {
        DataBaseItem item = null;
        try {
            item = new DataBaseItem(document, collectionName);
        } catch (ResolutionException | ReadValueException | InvalidArgumentException e) {
            throw new IDataBaseException("Failed to create DataBaseItem", e);
        }
        insert(item);
    }

    @Override
    public void update(final IObject document, final String collectionName) throws IDataBaseException {
        DataBaseItem item = null;
        try {
            item = new DataBaseItem(document, collectionName);
        } catch (ResolutionException | ReadValueException | InvalidArgumentException e) {
            throw new IDataBaseException("Failed to create DataBaseItem", e);
        }
        update(item);
    }

    private void update(final DataBaseItem item) {
        for (int i = 0; i < list.size(); i++) {
            DataBaseItem inBaseElem = list.get(i);
            if (inBaseElem.getId().equals(item.getId()) && inBaseElem.getCollectionName().equals(item.getCollectionName())) {
                list.remove(i);
                list.add(i, item);
            }
        }
    }

    private void insert(final DataBaseItem item) throws IDataBaseException {
        try {
            item.setId(nextId());
        } catch (ChangeValueException | InvalidArgumentException e) {
            throw new IDataBaseException("Failed to set id to DataBaseItem", e);
        }
        list.add(item);
    }

    @Override
    public IObject getById(final Object id, final String collectionName) {
        for (DataBaseItem item : list) {
            if (item.getId().equals(id) && item.getCollectionName().equals(collectionName)) {
                return item.getDocument();
            }
        }
        return null;
    }

    private Object nextId() {
        return list.size() + 1;
    }

    @Override
    public List<IObject> select(final IObject condition, final String collectionName) throws IDataBaseException {
        List<IObject> outputList = new LinkedList<>();
        for (DataBaseItem item : list) {
            if (Objects.equals(item.getCollectionName(), collectionName)) {
                if (verifierMap.get("$general").verify(condition, item.getDocument())) {
                    outputList.add(clone(item.getDocument()));
                }
            }
        }
        return outputList;
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
    public void delete(final IObject document, final String collectionName) {

    }
}
