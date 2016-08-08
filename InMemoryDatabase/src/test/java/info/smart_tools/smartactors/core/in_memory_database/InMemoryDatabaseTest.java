package info.smart_tools.smartactors.core.in_memory_database;

import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.idatabase.exception.IDataBaseException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.iobject.exception.SerializeException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class InMemoryDatabaseTest {

    @Before
    public void setUp() throws ResolutionException, InvalidArgumentException, RegistrationException, ScopeProviderException {
        ScopeProvider.subscribeOnCreationNewScope(
                scope -> {
                    try {
                        scope.setValue(IOC.getIocKey(), new StrategyContainer());
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }
        );

        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope mainScope = ScopeProvider.getScope(keyOfMainScope);
        ScopeProvider.setCurrentScope(mainScope);

        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy()
        );
        IOC.register(Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new FieldName((String) args[0]);
                            } catch (InvalidArgumentException ignored) {
                            }
                            return null;
                        }
                )
        );
        IOC.register(Keys.getOrAdd(IObject.class.getCanonicalName()),
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new DSObject((Map<IFieldName, Object>) args[0]);
                            } catch (InvalidArgumentException ignored) {
                            }
                            return null;
                        }
                )
        );
        IOC.register(Keys.getOrAdd(DSObject.class.getCanonicalName()),
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new DSObject((String) args[0]);
                            } catch (InvalidArgumentException ignored) {
                            }
                            return null;
                        }
                )
        );
        IOC.register(Keys.getOrAdd("NestedFieldName"), new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            IFieldName fieldName = (IFieldName) args[0];
                            IObject iObject = (IObject) args[1];
                            String string = fieldName.toString();
                            String[] strings = string.split("\\.");
                            Object bufObject = iObject;
                            try {
                                for (String fieldString : strings) {
                                    if (!(bufObject instanceof IObject)) {
                                        return null;
                                    }
                                    IFieldName bufFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), fieldString);
                                    bufObject = ((IObject) bufObject).getValue(bufFieldName);
                                    if (null == bufObject) {
                                        return null;
                                    }
                                }
                                return bufObject;
                            } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                            }
                            return null;
                        }
                )
        );
        Map<String, IConditionVerifier> verifierMap = new HashMap<>();


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
                        Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                        ;
                        Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$eq"));
                        return reference.equals(entry);
                    } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                    }
                    return false;
                }
        );
        verifierMap.put("$neq", (condition, document) -> {
                    IFieldName fieldName = condition.iterator().next().getKey();
                    try {
                        Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                        ;
                        Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$neq"));
                        return !reference.equals(entry);
                    } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                    }
                    return false;
                }
        );
        verifierMap.put("$gt", (condition, document) -> {
                    IFieldName fieldName = condition.iterator().next().getKey();
                    try {
                        Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                        ;
                        Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$gt"));
                        if (reference instanceof Long) {
                            Long longEntry = (Long) entry;
                            Long longReference = (Long) reference;
                            return longReference.compareTo(longEntry) == -1;
                        }
                        if (reference instanceof Double) {
                            Double doubleReference = (Double) reference;
                            Double doubleEntry = Double.parseDouble(entry.toString());
                            return doubleReference.compareTo(doubleEntry) == -1;
                        }
                        if (reference instanceof String) {
                            String doubleEntry = (String) entry;
                            String doubleReference = (String) reference;
                            return doubleReference.compareTo(doubleEntry) == -1;
                        }
                    } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                    }
                    return false;
                }
        );
        verifierMap.put("$lt", (condition, document) -> {
                    IFieldName fieldName = condition.iterator().next().getKey();
                    try {
                        Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                        ;
                        Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$lt"));
                        if (reference instanceof Long) {
                            Long longEntry = (Long) entry;
                            Long longReference = (Long) reference;
                            return longReference.compareTo(longEntry) == 1;
                        }
                        if (reference instanceof Double) {
                            Double doubleReference = (Double) reference;
                            Double doubleEntry = Double.parseDouble(entry.toString());
                            return doubleReference.compareTo(doubleEntry) == 1;
                        }
                        if (reference instanceof String) {
                            String doubleEntry = (String) entry;
                            String doubleReference = (String) reference;
                            return doubleReference.compareTo(doubleEntry) == 1;
                        }
                    } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                    }
                    return false;
                }
        );
        verifierMap.put("$gte", (condition, document) -> {
                    IFieldName fieldName = condition.iterator().next().getKey();
                    try {
                        Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                        ;
                        Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$gte"));
                        if (reference instanceof Long) {
                            Long longEntry = (Long) entry;
                            Long longReference = (Long) reference;
                            return longReference.compareTo(longEntry) == -1 || longReference.equals(longEntry);
                        }
                        if (reference instanceof Double) {
                            Double doubleReference = (Double) reference;
                            Double doubleEntry = Double.parseDouble(entry.toString());
                            return doubleReference.compareTo(doubleEntry) == -1 || doubleReference.equals(doubleEntry);
                        }
                        if (reference instanceof String) {
                            String doubleEntry = (String) entry;
                            String doubleReference = (String) reference;
                            return doubleReference.compareTo(doubleEntry) == -1 || doubleReference.equals(doubleEntry);
                        }
                    } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
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
                        return reference.compareTo(entry) == -1 || reference.equals(entry);
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
                        return reference.compareTo(entry) == 1 || reference.equals(entry);
                    } catch (ReadValueException | InvalidArgumentException e) {
                    }
                    return false;
                }
        );
        verifierMap.put("$lte", (condition, document) -> {
                    IFieldName fieldName = condition.iterator().next().getKey();
                    try {
                        Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                        ;
                        Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$lte"));
                        if (reference instanceof Long) {
                            Long longEntry = (Long) entry;
                            Long longReference = (Long) reference;
                            return longReference.compareTo(longEntry) == 1 || longReference.equals(longEntry);
                        }
                        if (reference instanceof Double) {
                            Double doubleReference = (Double) reference;
                            Double doubleEntry = Double.parseDouble(entry.toString());
                            return doubleReference.compareTo(doubleEntry) == 1 || doubleReference.equals(doubleEntry);
                        }
                        if (reference instanceof String) {
                            String doubleEntry = (String) entry;
                            String doubleReference = (String) reference;
                            return doubleReference.compareTo(doubleEntry) == 1 || doubleReference.equals(doubleEntry);
                        }
                    } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                    }
                    return false;
                }
        );
        verifierMap.put("$in", (condition, document) -> {
                    IFieldName fieldName = condition.iterator().next().getKey();
                    try {
                        Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                        ;
                        List<Object> references = (List<Object>)
                                ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$in"));
                        for (Object reference : references) {
                            if (reference.equals(entry)) {
                                return true;
                            }
                        }
                    } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                    }
                    return false;
                }
        );

        verifierMap.put("$isNull", (condition, document) -> {
                    IFieldName fieldName = condition.iterator().next().getKey();
                    try {
                        Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                        ;
                        Boolean references = (Boolean)
                                ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$isNull"));
                        return (null == entry) == references;
                    } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
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
        verifierMap.put("$hasTag", (condition, document) -> {
                    IFieldName fieldName = condition.iterator().next().getKey();
                    try {
                        Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                        if (null == entry) {
                            return false;
                        }
                        Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$hasTag"));
                        if (entry instanceof List) {
                            List<Object> entryList = (List<Object>) entry;
                            for (Object entryItem : entryList) {
                                if (entryItem.equals(reference)) {
                                    return true;
                                }
                            }
                            return false;
                        }

                        IFieldName tagFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), reference);
                        return null != ((IObject) entry).getValue(tagFieldName);
                    } catch (Exception e) {
                        return false;
                    }
                }
        );

        IOC.register(Keys.getOrAdd("ResolveDataBaseCondition"), new ApplyFunctionToArgumentsStrategy(
                        (args) ->
                                verifierMap.get(args[0]).verify((IObject) args[1], (IObject) args[2])
                )
        );
        IOC.register(Keys.getOrAdd("ContainsResolveDataBaseCondition"),
                new ApplyFunctionToArgumentsStrategy(
                        (args) ->
                                verifierMap.containsKey(args[0])
                )
        );
    }

    @Test
    public void testInsert_shouldAddId() throws InvalidArgumentException, IDataBaseException, ReadValueException {
        InMemoryDatabase database = new InMemoryDatabase();
        IObject document = new DSObject("{\"hello\": \"world\"}");
        database.insert(document, "collection_name");
        assertTrue(document.getValue(new FieldName("collection_nameID")).equals(1));
    }

    @Test
    public void testUpsertAsInsert_shouldAddId() throws InvalidArgumentException, IDataBaseException, ReadValueException {
        InMemoryDatabase database = new InMemoryDatabase();
        IObject document = new DSObject("{\"hello\": \"world\"}");
        database.upsert(document, "collection_name");
        assertTrue(document.getValue(new FieldName("collection_nameID")).equals(1));
    }

    @Test
    public void testUpsertAsUpdate_shouldNotChangeId() throws InvalidArgumentException, IDataBaseException, ReadValueException {
        InMemoryDatabase database = new InMemoryDatabase();
        IObject document = new DSObject("{\"hello\": \"world\"}");
        database.upsert(document, "collection_name");
        assertTrue(document.getValue(new FieldName("collection_nameID")).equals(1));
        database.upsert(document, "collection_name");
        assertTrue(document.getValue(new FieldName("collection_nameID")).equals(1));
    }

    @Test
    public void testUpdate_shouldNotChangeId() throws InvalidArgumentException, IDataBaseException, ReadValueException {
        InMemoryDatabase database = new InMemoryDatabase();
        IObject document = new DSObject("{\"hello\": \"world\"}");
        database.insert(document, "collection_name");
        assertTrue(document.getValue(new FieldName("collection_nameID")).equals(1));
        database.update(document, "collection_name");
        assertTrue(document.getValue(new FieldName("collection_nameID")).equals(1));
    }

    @Test
    public void testSearchEq() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        IObject document = new DSObject("{\"hello\": \"world\"}");
        IObject document2 = new DSObject("{\"hello\": \"world1\"}");
        database.insert(document, "collection_name");
        database.insert(document2, "collection_name");
        List<IObject> outputList =
                database.select(new DSObject("{\"hello\": {\"$eq\": \"world\"}}"), "collection_name");
        assertTrue(outputList.size() == 1);
        assertTrue(outputList.get(0).serialize().equals(document.serialize()));
    }

    @Test
    public void testSearchEqAtEmptyDB() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        List<IObject> outputList =
                database.select(new DSObject("{\"hello\": {\"$eq\": \"world\"}}"), "collection_name");
        assertTrue(outputList.size() == 0);
    }

    @Test
    public void testSearchAndEq() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        IObject document = new DSObject("{\"hello\": \"world\"}");
        IObject document2 = new DSObject("{\"hello\": \"world1\"}");
        database.insert(document, "collection_name");
        database.insert(document2, "collection_name");
        List<IObject> outputList =
                database.select(new DSObject("{\"$and\": [{\"hello\": {\"$eq\": \"world\"}}]}"), "collection_name");
        assertTrue(outputList.size() == 1);
        assertTrue(outputList.get(0).serialize().equals(document.serialize()));
    }

    @Test
    public void testSearchOrEq() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        IObject document = new DSObject("{\"hello\": \"world\"}");
        IObject document2 = new DSObject("{\"hello\": \"world1\"}");
        IObject document3 = new DSObject("{\"hello\": \"world2\"}");
        IObject document4 = new DSObject("{\"hello\": \"world3\"}");
        database.insert(document, "collection_name");
        database.insert(document2, "collection_name");
        database.insert(document3, "collection_name");
        database.insert(document4, "collection_name");
        List<IObject> outputList =
                database.select(
                        new DSObject("{\"$and\": [{\"$or\": [{\"hello\": {\"$eq\": \"world\"}}, {\"hello\": {\"$eq\": \"world2\"}}]}]}"),
                        "collection_name");
        assertTrue(outputList.size() == 2);
        assertTrue(outputList.get(0).serialize().equals(document.serialize()));
        assertTrue(outputList.get(1).serialize().equals(document3.serialize()));
    }

    @Test
    public void testSearchNotOrEq() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        IObject document = new DSObject("{\"hello\": \"world\"}");
        IObject document2 = new DSObject("{\"hello\": \"world1\"}");
        IObject document3 = new DSObject("{\"hello\": \"world2\"}");
        IObject document4 = new DSObject("{\"hello\": \"world3\"}");
        database.insert(document, "collection_name");
        database.insert(document2, "collection_name");
        database.insert(document3, "collection_name");
        database.insert(document4, "collection_name");
        List<IObject> outputList =
                database.select(
                        new DSObject("{\"$and\": [{\"$not\": [{\"$or\": [{\"hello\": {\"$eq\": \"world\"}}, {\"hello\": {\"$eq\": \"world2\"}}]}]}]}"),
                        "collection_name");
        assertTrue(outputList.size() == 2);
        assertTrue(outputList.get(0).serialize().equals(document2.serialize()));
        assertTrue(outputList.get(1).serialize().equals(document4.serialize()));
    }

    @Test
    public void testSearchGtForNumbers() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        IObject document = new DSObject("{\"a\": 1}");
        IObject document2 = new DSObject("{\"a\": 2}");
        IObject document3 = new DSObject("{\"a\": 3}");
        IObject document4 = new DSObject("{\"a\": 3.4}");
        database.insert(document, "collection_name");
        database.insert(document2, "collection_name");
        database.insert(document3, "collection_name");
        database.insert(document4, "collection_name");
        List<IObject> outputList =
                database.select(
                        new DSObject("{\"$and\": [{\"a\": {\"$gt\": 2.3}}]}"),
                        "collection_name");
        assertTrue(outputList.size() == 2);
        assertTrue(outputList.get(0).serialize().equals(document3.serialize()));
        assertTrue(outputList.get(1).serialize().equals(document4.serialize()));
    }

    @Test
    public void testSearchLtForNumbers() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        IObject document = new DSObject("{\"a\": 1}");
        IObject document2 = new DSObject("{\"a\": 2.3}");
        IObject document3 = new DSObject("{\"a\": 3}");
        IObject document4 = new DSObject("{\"a\": 3.4}");
        database.insert(document, "collection_name");
        database.insert(document2, "collection_name");
        database.insert(document3, "collection_name");
        database.insert(document4, "collection_name");
        List<IObject> outputList =
                database.select(
                        new DSObject("{\"$and\": [{\"a\": {\"$lt\": 2.3}}]}"),
                        "collection_name");
        assertTrue(outputList.size() == 1);
        assertTrue(outputList.get(0).serialize().equals(document.serialize()));
    }

    @Test
    public void testSearchGteForNumbers() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        IObject document = new DSObject("{\"a\": 1}");
        IObject document2 = new DSObject("{\"a\": 2.3}");
        IObject document3 = new DSObject("{\"a\": 3}");
        IObject document4 = new DSObject("{\"a\": 3.4}");
        database.insert(document, "collection_name");
        database.insert(document2, "collection_name");
        database.insert(document3, "collection_name");
        database.insert(document4, "collection_name");
        List<IObject> outputList =
                database.select(
                        new DSObject("{\"$and\": [{\"a\": {\"$gte\": 2.3}}]}"),
                        "collection_name");
        assertTrue(outputList.size() == 3);
        assertTrue(outputList.get(0).serialize().equals(document2.serialize()));
        assertTrue(outputList.get(1).serialize().equals(document3.serialize()));
        assertTrue(outputList.get(2).serialize().equals(document4.serialize()));
    }

    @Test
    public void testSearchLteForNumbers() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        IObject document = new DSObject("{\"a\": 1}");
        IObject document2 = new DSObject("{\"a\": 2.3}");
        IObject document3 = new DSObject("{\"a\": 3}");
        IObject document4 = new DSObject("{\"a\": 3.4}");
        database.insert(document, "collection_name");
        database.insert(document2, "collection_name");
        database.insert(document3, "collection_name");
        database.insert(document4, "collection_name");
        List<IObject> outputList =
                database.select(
                        new DSObject("{\"$and\": [{\"a\": {\"$lte\": 2.3}}]}"),
                        "collection_name");
        assertTrue(outputList.size() == 2);
        assertTrue(outputList.get(0).serialize().equals(document.serialize()));
        assertTrue(outputList.get(1).serialize().equals(document2.serialize()));
    }

    @Test
    public void testSearchIn() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        IObject document = new DSObject("{\"a\": 1}");
        IObject document2 = new DSObject("{\"a\": 2.3}");
        IObject document3 = new DSObject("{\"a\": 3}");
        IObject document4 = new DSObject("{\"a\": 3.4}");
        database.insert(document, "collection_name");
        database.insert(document2, "collection_name");
        database.insert(document3, "collection_name");
        database.insert(document4, "collection_name");
        List<IObject> outputList =
                database.select(
                        new DSObject("{\"$and\": [{\"a\": {\"$in\": [2.3, 3, 4, 5]}}]}"),
                        "collection_name");
        assertTrue(outputList.size() == 2);
        assertTrue(outputList.get(0).serialize().equals(document2.serialize()));
        assertTrue(outputList.get(1).serialize().equals(document3.serialize()));
    }

    @Test
    public void testNestedEq() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        IObject document = new DSObject("{\"a\": {\"b\": 1}}");
        IObject document2 = new DSObject("{\"a\": {\"b\": 2}}");
        IObject document3 = new DSObject("{\"a\": 3}");
        IObject document4 = new DSObject("{\"a\": 3.4}");
        database.insert(document, "collection_name");
        database.insert(document2, "collection_name");
        database.insert(document3, "collection_name");
        database.insert(document4, "collection_name");
        List<IObject> outputList =
                database.select(
                        new DSObject("{\"$and\": [{\"a.b\": {\"$eq\": 1}}]}"),
                        "collection_name");
        assertTrue(outputList.size() == 1);
        assertTrue(outputList.get(0).serialize().equals(document.serialize()));
    }

    @Test
    public void testIsNull() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        IObject document = new DSObject("{\"a\": {\"b\": 1}}");
        IObject document2 = new DSObject("{\"c\": {\"b\": 2}}");
        IObject document3 = new DSObject("{\"c\": 3}");
        IObject document4 = new DSObject("{\"a\": 3.4}");
        database.insert(document, "collection_name");
        database.insert(document2, "collection_name");
        database.insert(document3, "collection_name");
        database.insert(document4, "collection_name");
        List<IObject> outputList =
                database.select(
                        new DSObject("{\"$and\": [{\"a\": {\"$isNull\": true}}]}"),
                        "collection_name");
        assertTrue(outputList.size() == 2);
        assertTrue(outputList.get(0).serialize().equals(document2.serialize()));
        assertTrue(outputList.get(1).serialize().equals(document3.serialize()));
    }

    @Test
    public void testHasTagField() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        IObject document = new DSObject("{\"a\": {\"b\": 1}}");
        IObject document2 = new DSObject("{\"c\": {\"b\": 2}}");
        IObject document3 = new DSObject("{\"c\": 3}");
        IObject document4 = new DSObject("{\"a\": 3.4}");
        database.insert(document, "collection_name");
        database.insert(document2, "collection_name");
        database.insert(document3, "collection_name");
        database.insert(document4, "collection_name");
        List<IObject> outputList =
                database.select(
                        new DSObject("{\"$and\": [{\"a\": {\"$hasTag\": \"b\"}}]}"),
                        "collection_name");
        assertTrue(outputList.size() == 1);
        assertTrue(outputList.get(0).serialize().equals(document.serialize()));
    }
    @Test
    public void testHasTagAtList() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        IObject document = new DSObject("{\"a\": [\"b\", 1]}");
        IObject document2 = new DSObject("{\"c\": [\"b\", 2]}");
        IObject document3 = new DSObject("{\"c\": 3}");
        IObject document4 = new DSObject("{\"a\": {\"b\": 3}}");
        database.insert(document, "collection_name");
        database.insert(document2, "collection_name");
        database.insert(document3, "collection_name");
        database.insert(document4, "collection_name");
        List<IObject> outputList =
                database.select(
                        new DSObject("{\"$and\": [{\"a\": {\"$hasTag\": \"b\"}}]}"),
                        "collection_name");
        assertTrue(outputList.size() == 2);
        assertTrue(outputList.get(0).serialize().equals(document.serialize()));
        assertTrue(outputList.get(1).serialize().equals(document4.serialize()));
    }
}
