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
import org.junit.Before;
import org.junit.Test;

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
}
