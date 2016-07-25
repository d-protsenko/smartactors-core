package info.smart_tools.smartactors.core.postgres_upsert_task;

import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for PostgresUpsertTask.
 */
public class PostgresUpsertTaskTest {

    private StorageConnection connection;
    private IDatabaseTask task;
    private UpsertMessage message;
    private IObject document;
    private IFieldName idFieldName;

    @BeforeClass
    public static void prepareIOC() throws ScopeProviderException, InvalidArgumentException, RegistrationException, ResolutionException {
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
                new ResolveByNameIocStrategy(
                        (a) -> {
                            try {
                                return new Key((String) a[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );
        IOC.register(
                Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                new CreateNewInstanceStrategy(
                        (arg) -> {
                            try {
                                return new FieldName(String.valueOf(arg[0]));
                            } catch (InvalidArgumentException ignored) {
                                return null;
                            }
                        }
                )
        );
    }

    @Before
    public void setUp() throws QueryBuildException, InvalidArgumentException, ResolutionException, RegistrationException {
        connection = mock(StorageConnection.class);
        task = new PostgresUpsertTask();
        document = mock(IObject.class);
        message = mock(UpsertMessage.class);
        when(message.getCollectionName()).thenReturn(CollectionName.fromString("test"));
        when(message.getDocument()).thenReturn(document);
        idFieldName = new FieldName("testID");

        IOC.register(
                Keys.getOrAdd(UpsertMessage.class.getCanonicalName()),
                new SingletonStrategy(message)
        );
    }

    @Test
    public void testInsert() throws InvalidArgumentException, ReadValueException, TaskPrepareException, TaskSetConnectionException, TaskExecutionException, ChangeValueException {
        FieldName testFieldName = new FieldName("testField");
        when(document.getValue(eq(testFieldName))).thenReturn("testValue");
        task.setConnection(connection);
        task.prepare(message);
        task.execute();
        verify(document).setValue(eq(idFieldName), any());
    }

}
