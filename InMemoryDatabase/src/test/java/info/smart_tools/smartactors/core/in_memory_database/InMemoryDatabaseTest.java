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
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import org.junit.Before;
import org.junit.Test;

import static com.sun.javaws.JnlpxArgs.verify;
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

}
