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
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class InMemoryDatabaseTest {

    @BeforeClass
    public static void setUp() throws ResolutionException, InvalidArgumentException, RegistrationException, ScopeProviderException {
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
        Map<String, IConditionVerifier> verifierMap = new HashMap<>();
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
        IOC.register(Keys.getOrAdd("CompareSimpleObjects"), new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            Object entry = args[0];
                            Object reference = args[1];
                            if (null == entry) {
                                return -1;
                            }
                            if (null == reference) {
                                return 1;
                            }
                            if (reference instanceof Long || reference instanceof Integer) {
                                Long longEntry = Long.parseLong(String.valueOf(entry));
                                Long longReference = Long.parseLong(String.valueOf(reference));
                                return -longReference.compareTo(longEntry);
                            }
                            if (reference instanceof Double) {
                                Double doubleReference = (Double) reference;
                                Double doubleEntry = Double.parseDouble(entry.toString());
                                return -doubleReference.compareTo(doubleEntry);
                            }
                            if (reference instanceof String) {
                                String doubleEntry = (String) entry;
                                String doubleReference = (String) reference;
                                return -doubleReference.compareTo(doubleEntry);
                            }
                            return 0;
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
                        Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$gt"));
                        return ((Integer) IOC.resolve(
                                Keys.getOrAdd("CompareSimpleObjects"), entry, reference)
                        ) > 0;
                    } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                    }
                    return false;
                }
        );
        verifierMap.put("$lt", (condition, document) -> {
                    IFieldName fieldName = condition.iterator().next().getKey();
                    try {
                        Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                        Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$lt"));
                        return (Integer) IOC.resolve(
                                Keys.getOrAdd("CompareSimpleObjects"), entry, reference
                        ) < 0;
                    } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                    }
                    return false;
                }
        );
        verifierMap.put("$gte", (condition, document) -> {
                    IFieldName fieldName = condition.iterator().next().getKey();
                    try {
                        Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                        Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$gte"));
                        return (Integer) IOC.resolve(
                                Keys.getOrAdd("CompareSimpleObjects"), entry, reference
                        ) >= 0;
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
                        Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$lte"));
                        return (Integer) IOC.resolve(
                                Keys.getOrAdd("CompareSimpleObjects"), entry, reference
                        ) <= 0;
                    } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                    }
                    return false;
                }
        );
        verifierMap.put("$in", (condition, document) -> {
                    IFieldName fieldName = condition.iterator().next().getKey();
                    try {
                        Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
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
        IOC.register(Keys.getOrAdd("PagingForDatabaseCollection"),
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            IObject page = null;
                            Integer pageNumber = 0;
                            Integer pageSize = 0;
                            try {
                                IFieldName pageFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "page");
                                IFieldName pageNumberFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "number");
                                IFieldName pageSizeFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "size");
                                page = (IObject) ((IObject) args[0]).getValue(pageFieldName);
                                if (null == page) {
                                    pageNumber = 1;
                                    pageSize = 100;
                                } else {
                                    pageNumber = (Integer) page.getValue(pageNumberFieldName);
                                    pageSize = (Integer) page.getValue(pageSizeFieldName);
                                }
                            } catch (ResolutionException | ReadValueException | InvalidArgumentException e) {
                            }
                            Integer startNumber = (pageNumber - 1) * pageSize;
                            Integer finNumber = pageNumber * pageSize;
                            List<IObject> outputDocuments = new LinkedList<>();
                            List<IObject> documents = (List<IObject>) args[1];
                            for (int i = startNumber; i < finNumber && i < documents.size(); i++) {
                                outputDocuments.add(documents.get(i));
                            }
                            return outputDocuments;
                        }
                )
        );

        IOC.register(Keys.getOrAdd("SortIObjects"), new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            IObject condition = (IObject) args[0];
                            List<IObject> documents = (List<IObject>) args[1];

                            IFieldName sortFieldName = null;
                            try {
                                sortFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "sort");
                            } catch (ResolutionException e) {
                                e.printStackTrace();
                            }
                            List<IObject> sortRules = null;
                            try {
                                sortRules = (List<IObject>) condition.getValue(sortFieldName);
                            } catch (ReadValueException | InvalidArgumentException e) {
                                e.printStackTrace();
                            }
                            if (null == sortRules) {
                                return documents;
                            }
                            List<IFieldName> sortingFields = new LinkedList<>();
                            List<Integer> sortingType = new LinkedList<>();
                            for (IObject sortRule : sortRules) {
                                sortingFields.add(sortRule.iterator().next().getKey());
                                sortingType.add(sortRule.iterator().next().getValue().equals("asc") ? 1 : -1);
                            }
                            Comparator comparator = (o1, o2) -> {
                                try {
                                    Integer compare = 0;
                                    for (int i = 0; i < sortingFields.size(); i++) {
                                        Object object1 = IOC.resolve(Keys.getOrAdd("NestedFieldName"), sortingFields.get(i), o1);
                                        Object object2 = IOC.resolve(Keys.getOrAdd("NestedFieldName"), sortingFields.get(i), o2);
                                        compare = IOC.resolve(Keys.getOrAdd("CompareSimpleObjects"), object1, object2);
                                        if (compare != 0) {
                                            return compare * sortingType.get(i);
                                        }
                                    }
                                } catch (ResolutionException e) {
                                }
                                return 0;
                            };
                            documents.sort(comparator);
                            return documents;

                        }
                )
        );

        IOC.register(Keys.getOrAdd(DataBaseItem.class.getCanonicalName()), new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new DataBaseItem((IObject) args[0], (String) args[1]);
                            } catch (ResolutionException | ReadValueException | InvalidArgumentException e) {
                            }
                            return null;
                        }
                )
        );

    }

    @Test
    public void testInsert_shouldAddId() throws InvalidArgumentException, IDataBaseException, ReadValueException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
        IObject document = new DSObject("{\"hello\": \"world\"}");
        database.insert(document, "collection_name");
        assertTrue(document.getValue(new FieldName("collection_nameID")).equals(1));
    }

    @Test
    public void testUpsertAsInsert_shouldAddId() throws InvalidArgumentException, IDataBaseException, ReadValueException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
        IObject document = new DSObject("{\"hello\": \"world\"}");
        database.upsert(document, "collection_name");
        assertTrue(document.getValue(new FieldName("collection_nameID")).equals(1));
    }

    @Test
    public void testUpsertAsUpdate_shouldNotChangeId() throws InvalidArgumentException, IDataBaseException, ReadValueException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
        IObject document = new DSObject("{\"hello\": \"world\"}");
        database.upsert(document, "collection_name");
        assertTrue(document.getValue(new FieldName("collection_nameID")).equals(1));
        database.upsert(document, "collection_name");
        assertTrue(document.getValue(new FieldName("collection_nameID")).equals(1));
    }

    @Test
    public void testUpdate_shouldNotChangeId() throws InvalidArgumentException, IDataBaseException, ReadValueException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
        IObject document = new DSObject("{\"hello\": \"world\"}");
        database.insert(document, "collection_name");
        assertTrue(document.getValue(new FieldName("collection_nameID")).equals(1));
        database.update(document, "collection_name");
        assertTrue(document.getValue(new FieldName("collection_nameID")).equals(1));
    }

    @Test
    public void testSearchEq() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
        IObject document = new DSObject("{\"hello\": \"world\"}");
        IObject document2 = new DSObject("{\"hello\": \"world1\"}");
        database.insert(document, "collection_name");
        database.insert(document2, "collection_name");
        List<IObject> outputList =
                database.select(new DSObject("{\"filter\":{\"hello\": {\"$eq\": \"world\"}}}"), "collection_name");
        assertTrue(outputList.size() == 1);
        assertTrue(outputList.get(0).serialize().equals(document.serialize()));
    }

    @Test
    public void testSearchEqAtEmptyDB() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
        List<IObject> outputList =
                database.select(new DSObject("{\"filter\":{\"hello\": {\"$eq\": \"world\"}}}"), "collection_name");
        assertTrue(outputList.size() == 0);
    }

    @Test
    public void testSearchAndEq() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
        IObject document = new DSObject("{\"hello\": \"world\"}");
        IObject document2 = new DSObject("{\"hello\": \"world1\"}");
        database.insert(document, "collection_name");
        database.insert(document2, "collection_name");
        List<IObject> outputList =
                database.select(new DSObject("{\"filter\":{\"$and\": [{\"hello\": {\"$eq\": \"world\"}}]}}"), "collection_name");
        assertTrue(outputList.size() == 1);
        assertTrue(outputList.get(0).serialize().equals(document.serialize()));
    }

    @Test
    public void testSearchOrEq() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
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
                        new DSObject("{\"filter\":{\"$and\": [{\"$or\": [{\"hello\": {\"$eq\": \"world\"}}, {\"hello\": {\"$eq\": \"world2\"}}]}]}}"),
                        "collection_name");
        assertTrue(outputList.size() == 2);
        assertTrue(outputList.get(0).serialize().equals(document.serialize()));
        assertTrue(outputList.get(1).serialize().equals(document3.serialize()));
    }

    @Test
    public void testSearchNotOrEq() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
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
                        new DSObject("{\"filter\":{\"$and\": [{\"$not\": [{\"$or\": [{\"hello\": {\"$eq\": \"world\"}}, {\"hello\": {\"$eq\": \"world2\"}}]}]}]}}"),
                        "collection_name");
        assertTrue(outputList.size() == 2);
        assertTrue(outputList.get(0).serialize().equals(document2.serialize()));
        assertTrue(outputList.get(1).serialize().equals(document4.serialize()));
    }

    @Test
    public void testSearchGtForNumbers() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
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
                        new DSObject("{\"filter\":{\"$and\": [{\"a\": {\"$gt\": 2.3}}]}}"),
                        "collection_name");
        assertTrue(outputList.size() == 2);
        assertTrue(outputList.get(0).serialize().equals(document3.serialize()));
        assertTrue(outputList.get(1).serialize().equals(document4.serialize()));
    }

    @Test
    public void testSearchLtForNumbers() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
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
                        new DSObject("{\"filter\":{\"$and\": [{\"a\": {\"$lt\": 2.3}}]}}"),
                        "collection_name");
        assertTrue(outputList.size() == 1);
        assertTrue(outputList.get(0).serialize().equals(document.serialize()));
    }

    @Test
    public void testSearchGteForNumbers() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
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
                        new DSObject("{\"filter\":{\"$and\": [{\"a\": {\"$gte\": 2.3}}]}}"),
                        "collection_name");
        assertTrue(outputList.size() == 3);
        assertTrue(outputList.get(0).serialize().equals(document2.serialize()));
        assertTrue(outputList.get(1).serialize().equals(document3.serialize()));
        assertTrue(outputList.get(2).serialize().equals(document4.serialize()));
    }

    @Test
    public void testSearchLteForNumbers() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
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
                        new DSObject("{\"filter\":{\"$and\": [{\"a\": {\"$lte\": 2.3}}]}}"),
                        "collection_name");
        assertTrue(outputList.size() == 2);
        assertTrue(outputList.get(0).serialize().equals(document.serialize()));
        assertTrue(outputList.get(1).serialize().equals(document2.serialize()));
    }

    @Test
    public void testSearchIn() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
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
                        new DSObject("{\"filter\":{\"$and\": [{\"a\": {\"$in\": [2.3, 3, 4, 5]}}]}}"),
                        "collection_name");
        assertTrue(outputList.size() == 2);
        assertTrue(outputList.get(0).serialize().equals(document2.serialize()));
        assertTrue(outputList.get(1).serialize().equals(document3.serialize()));
    }

    @Test
    public void testNestedEq() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
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
                        new DSObject("{\"filter\":{\"$and\": [{\"a.b\": {\"$eq\": 1}}]}}"),
                        "collection_name");
        assertTrue(outputList.size() == 1);
        assertTrue(outputList.get(0).serialize().equals(document.serialize()));
    }

    @Test
    public void testIsNull() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
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
                        new DSObject("{\"filter\":{\"$and\": [{\"a\": {\"$isNull\": true}}]}}"),
                        "collection_name");
        assertTrue(outputList.size() == 2);
        assertTrue(outputList.get(0).serialize().equals(document2.serialize()));
        assertTrue(outputList.get(1).serialize().equals(document3.serialize()));
    }

    @Test
    public void testHasTagField() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
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
                        new DSObject("{\"filter\":{\"$and\": [{\"a\": {\"$hasTag\": \"b\"}}]}}"),
                        "collection_name");
        assertTrue(outputList.size() == 1);
        assertTrue(outputList.get(0).serialize().equals(document.serialize()));
    }

    @Test
    public void testHasTagAtList() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
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
                        new DSObject("{\"filter\":{\"$and\": [{\"a\": {\"$hasTag\": \"b\"}}]}}"),
                        "collection_name");
        assertTrue(outputList.size() == 2);
        assertTrue(outputList.get(0).serialize().equals(document.serialize()));
        assertTrue(outputList.get(1).serialize().equals(document4.serialize()));
    }

    @Test
    public void testPagingOneElem() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
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
                        new DSObject("{\"filter\":{}, \"page\": {\"size\": 1, \"number\":1}}"),
                        "collection_name");
        assertTrue(outputList.size() == 1);
        assertTrue(outputList.get(0).serialize().equals(document.serialize()));
    }

    @Test
    public void testPagingSomeElems() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
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
                        new DSObject("{\"filter\":{}, \"page\": {\"size\": 2, \"number\":1}}"),
                        "collection_name");
        assertTrue(outputList.size() == 2);
        assertTrue(outputList.get(0).serialize().equals(document.serialize()));
        assertTrue(outputList.get(1).serialize().equals(document2.serialize()));
    }

    @Test
    public void testPagingWithEmptyResult() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
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
                        new DSObject("{\"filter\":{}, \"page\": {\"size\": 1, \"number\":5}}"),
                        "collection_name");
        assertTrue(outputList.size() == 0);
    }

    @Test
    public void testSortingAscOneField() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
        IObject document = new DSObject("{\"c\": 0}");
        IObject document2 = new DSObject("{\"c\": 5}");
        IObject document3 = new DSObject("{\"c\": 3}");
        IObject document4 = new DSObject("{\"c\": 4}");
        IObject document5 = new DSObject("{\"c\": 1}");
        database.insert(document, "collection_name");
        database.insert(document2, "collection_name");
        database.insert(document3, "collection_name");
        database.insert(document4, "collection_name");
        database.insert(document5, "collection_name");
        List<IObject> outputList =
                database.select(
                        new DSObject("{\"sort\": [{\"c\": \"asc\"}]}"),
                        "collection_name");
        assertTrue(outputList.size() == 5);
        assertTrue(outputList.get(0).serialize().equals(document.serialize()));
        assertTrue(outputList.get(1).serialize().equals(document5.serialize()));
        assertTrue(outputList.get(2).serialize().equals(document3.serialize()));
        assertTrue(outputList.get(3).serialize().equals(document4.serialize()));
        assertTrue(outputList.get(4).serialize().equals(document2.serialize()));
    }

    @Test
    public void testSortingDescOneField() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
        IObject document = new DSObject("{\"c\": 0}");
        IObject document2 = new DSObject("{\"c\": 5}");
        IObject document3 = new DSObject("{\"c\": 3}");
        IObject document4 = new DSObject("{\"c\": 4}");
        IObject document5 = new DSObject("{\"c\": 1}");
        database.insert(document, "collection_name");
        database.insert(document2, "collection_name");
        database.insert(document3, "collection_name");
        database.insert(document4, "collection_name");
        database.insert(document5, "collection_name");
        List<IObject> outputList =
                database.select(
                        new DSObject("{\"sort\": [{\"c\": \"desc\"}]}"),
                        "collection_name");
        assertTrue(outputList.size() == 5);
        assertTrue(outputList.get(0).serialize().equals(document2.serialize()));
        assertTrue(outputList.get(1).serialize().equals(document4.serialize()));
        assertTrue(outputList.get(2).serialize().equals(document3.serialize()));
        assertTrue(outputList.get(3).serialize().equals(document5.serialize()));
        assertTrue(outputList.get(4).serialize().equals(document.serialize()));
    }

    @Test
    public void testSortingByNotExistingField() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
        IObject document = new DSObject("{}");
        IObject document2 = new DSObject("{\"c\": 5}");
        IObject document3 = new DSObject("{\"c\": 3}");
        IObject document4 = new DSObject("{\"c\": 4}");
        IObject document5 = new DSObject("{\"c\": 1}");
        database.insert(document, "collection_name");
        database.insert(document2, "collection_name");
        database.insert(document3, "collection_name");
        database.insert(document4, "collection_name");
        database.insert(document5, "collection_name");
        List<IObject> outputList =
                database.select(
                        new DSObject("{\"sort\": [{\"c\": \"desc\"}]}"),
                        "collection_name");
        assertTrue(outputList.size() == 5);
        assertTrue(outputList.get(0).serialize().equals(document2.serialize()));
        assertTrue(outputList.get(1).serialize().equals(document4.serialize()));
        assertTrue(outputList.get(2).serialize().equals(document3.serialize()));
        assertTrue(outputList.get(3).serialize().equals(document5.serialize()));
        assertTrue(outputList.get(4).serialize().equals(document.serialize()));
    }

    @Test
    public void testSortingByManyFields() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
        IObject document = new DSObject("{}");
        IObject document2 = new DSObject("{\"c\": 5, \"a\": 3}");
        IObject document3 = new DSObject("{\"c\": 5, \"a\": 2}");
        IObject document4 = new DSObject("{\"c\": 4}");
        IObject document5 = new DSObject("{\"c\": 3, \"a\": 4}");
        IObject document6 = new DSObject("{\"c\": 3, \"a\": 2}");
        database.insert(document, "collection_name");
        database.insert(document2, "collection_name");
        database.insert(document3, "collection_name");
        database.insert(document4, "collection_name");
        database.insert(document5, "collection_name");
        database.insert(document6, "collection_name");
        List<IObject> outputList =
                database.select(
                        new DSObject("{\"sort\": [{\"c\": \"desc\"}, {\"a\": \"asc\"}]}"),
                        "collection_name");
        assertTrue(outputList.size() == 6);
        assertTrue(outputList.get(0).serialize().equals(document3.serialize()));
        assertTrue(outputList.get(1).serialize().equals(document2.serialize()));
        assertTrue(outputList.get(2).serialize().equals(document4.serialize()));
        assertTrue(outputList.get(3).serialize().equals(document6.serialize()));
        assertTrue(outputList.get(4).serialize().equals(document5.serialize()));
    }

    @Test
    public void testDeleteElem() throws InvalidArgumentException, IDataBaseException, SerializeException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
        IObject document = new DSObject("{}");
        IObject document2 = new DSObject("{\"c\": 5, \"a\": 3}");
        IObject document3 = new DSObject("{\"c\": 5, \"a\": 2}");
        IObject document4 = new DSObject("{\"c\": 4}");
        IObject document5 = new DSObject("{\"c\": 3, \"a\": 4}");
        IObject document6 = new DSObject("{\"c\": 3, \"a\": 2}");
        database.insert(document, "collection_name");
        database.insert(document2, "collection_name");
        database.insert(document3, "collection_name");
        database.insert(document4, "collection_name");
        database.insert(document5, "collection_name");
        database.insert(document6, "collection_name");
        database.delete(document, "collection_name");
        List<IObject> outputList =
                database.select(
                        new DSObject(),
                        "collection_name");
        assertTrue(outputList.size() == 5);
    }

    @Test
    public void testDeleteIdFieldAfterDeleteDocument() throws InvalidArgumentException, IDataBaseException, SerializeException, ReadValueException {
        InMemoryDatabase database = new InMemoryDatabase();
        database.createCollection("collection_name");
        IObject document = new DSObject("{}");
        IObject document2 = new DSObject("{\"c\": 5, \"a\": 3}");
        IObject document3 = new DSObject("{\"c\": 5, \"a\": 2}");
        IObject document4 = new DSObject("{\"c\": 4}");
        IObject document5 = new DSObject("{\"c\": 3, \"a\": 4}");
        IObject document6 = new DSObject("{\"c\": 3, \"a\": 2}");
        database.insert(document, "collection_name");
        database.insert(document2, "collection_name");
        database.insert(document3, "collection_name");
        database.insert(document4, "collection_name");
        database.insert(document5, "collection_name");
        database.insert(document6, "collection_name");
        database.delete(document, "collection_name");
        assertTrue(null == document.getValue(new FieldName("collection_nameID")));
    }

    @Test(expected = IDataBaseException.class)
    public void testInsertToNotExistingCollection() throws InvalidArgumentException, IDataBaseException, SerializeException, ReadValueException {
        InMemoryDatabase database = new InMemoryDatabase();
        IObject document = new DSObject("{}");
        database.insert(document, "collection_name");
    }

    @Test(expected = IDataBaseException.class)
    public void testUpsertToNotExistingCollection() throws InvalidArgumentException, IDataBaseException, SerializeException, ReadValueException {
        InMemoryDatabase database = new InMemoryDatabase();
        IObject document = new DSObject("{}");
        database.upsert(document, "collection_name");
    }
}
