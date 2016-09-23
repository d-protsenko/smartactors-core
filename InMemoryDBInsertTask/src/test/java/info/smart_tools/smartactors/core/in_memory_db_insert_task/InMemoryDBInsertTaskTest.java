package info.smart_tools.smartactors.core.in_memory_db_insert_task;

import info.smart_tools.smartactors.base.strategy.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.idatabase.exception.IDatabaseException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.in_memory_database.InMemoryDatabase;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class InMemoryDBInsertTaskTest {

    private InMemoryDatabase inMemoryDatabase;

    @Before
    public void setUp() throws ScopeProviderException, ResolutionException, RegistrationException, InvalidArgumentException {
        inMemoryDatabase = mock(InMemoryDatabase.class);
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

        IOC.register(Keys.getOrAdd(InMemoryDatabase.class.getCanonicalName()), new SingletonStrategy(
                        inMemoryDatabase
                )
        );
    }

    @Test
    public void testPrepare() throws InvalidArgumentException, TaskPrepareException, TaskExecutionException, ChangeValueException {
        InMemoryDBInsertTask insertTask = new InMemoryDBInsertTask();
        IObject query = new DSObject("{\"collectionName\": \"collection_name\"}");
        IObject document = new DSObject("{\"hello\": \"world\"}");
        IFieldName documentFieldName = new FieldName("document");
        query.setValue(documentFieldName, document);
        insertTask.prepare(query);
    }

    @Test
    public void testExecute() throws InvalidArgumentException, ChangeValueException, TaskPrepareException, TaskExecutionException, IDatabaseException {
        InMemoryDBInsertTask insertTask = new InMemoryDBInsertTask();
        IObject query = new DSObject("{\"collectionName\": \"collection_name\"}");
        IObject document = new DSObject("{\"hello\": \"world\"}");
        IFieldName documentFieldName = new FieldName("document");
        query.setValue(documentFieldName, document);
        insertTask.prepare(query);
        insertTask.execute();
        verify(inMemoryDatabase).insert(document, "collection_name");
    }
}
