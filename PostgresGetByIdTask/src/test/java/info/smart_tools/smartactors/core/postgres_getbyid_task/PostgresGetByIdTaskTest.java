package info.smart_tools.smartactors.core.postgres_getbyid_task;

import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.iaction.IAction;
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
import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.core.istorage_connection.exception.StorageException;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_connection.JDBCCompiledQuery;
import info.smart_tools.smartactors.core.postgres_connection.QueryStatement;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for PostgresGetByIdTask.
 */
public class PostgresGetByIdTaskTest {

    private IDatabaseTask task;
    private GetByIdMessage message;
    private IFieldName idFieldName;
    private IStorageConnection connection;
    private JDBCCompiledQuery compiledQuery;
    private PreparedStatement statement;
    private ResultSet resultSet;

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
                        (args) -> {
                            try {
                                return new Key((String) args[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );
        IOC.register(
                Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new FieldName(String.valueOf(args[0]));
                            } catch (InvalidArgumentException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
        );
        IOC.register(
                Keys.getOrAdd(IObject.class.getCanonicalName()),
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new DSObject(String.valueOf(args[0]));
                            } catch (InvalidArgumentException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
        );
    }

    @Before
    public void setUp() throws QueryBuildException, InvalidArgumentException, ResolutionException, RegistrationException, ReadValueException, StorageException, SQLException {
        resultSet = mock(ResultSet.class);
        statement = mock(PreparedStatement.class);
        when(statement.getResultSet()).thenReturn(resultSet);
        compiledQuery = mock(JDBCCompiledQuery.class);
        when(compiledQuery.getPreparedStatement()).thenReturn(statement);
        connection = mock(IStorageConnection.class);
        when(connection.compileQuery(any())).thenReturn(compiledQuery);
        task = new PostgresGetByIdTask(connection);
        message = mock(GetByIdMessage.class);
        when(message.getCollectionName()).thenReturn(CollectionName.fromString("test"));
        idFieldName = new FieldName("testID");

        IOC.register(
                Keys.getOrAdd(GetByIdMessage.class.getCanonicalName()),
                new SingletonStrategy(message)
        );
    }

    @Test
    public void testGetById() throws InvalidArgumentException, ReadValueException, TaskPrepareException, TaskSetConnectionException, TaskExecutionException, ChangeValueException, StorageException, SQLException {
        when(message.getId()).thenReturn(123L);
        final IObject[] result = new IObject[1];
        when(message.getCallback()).thenReturn(doc -> result[0] = doc);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("{ \"testID\": 123, \"test\": \"value\" }");

        task.prepare(null); // the message will be resolved by IOC
        task.execute();

        verify(connection).compileQuery(any(QueryStatement.class));
        // implementation details of PostgresConnection
        // verify(statement).setLong(eq(1), eq(123L));
        verify(statement).execute();
        verify(resultSet).next();
        verify(connection).commit();
        assertEquals(123, result[0].getValue(idFieldName));
        assertEquals("value", result[0].getValue(new FieldName("test")));
    }

    @Test
    public void testGetByIdFailure() throws InvalidArgumentException, ReadValueException, TaskPrepareException, TaskSetConnectionException, TaskExecutionException, ChangeValueException, StorageException, SQLException {
        when(message.getId()).thenReturn(123L);
        IAction<IObject> callback = mock(IAction.class);
        when(message.getCallback()).thenReturn(callback);
        when(statement.execute()).thenThrow(SQLException.class);

        task.prepare(null); // the message will be resolved by IOC
        try {
            task.execute();
            fail();
        } catch (TaskExecutionException e) {
            // pass
        }

        verify(connection).compileQuery(any(QueryStatement.class));
        // implementation details of PostgresConnection
        // verify(statement).setLong(eq(1), eq(123L));
        verify(statement).execute();
        verifyZeroInteractions(resultSet);
        verifyZeroInteractions(callback);
        verify(connection).rollback();
    }

    @Test
    public void testGetByIdNotFound() throws InvalidArgumentException, ReadValueException, TaskPrepareException, TaskSetConnectionException, TaskExecutionException, ChangeValueException, StorageException, SQLException {
        when(message.getId()).thenReturn(123L);
        IAction<IObject> callback = mock(IAction.class);
        when(message.getCallback()).thenReturn(callback);
        when(resultSet.next()).thenReturn(false);
        when(resultSet.getString(anyInt())).thenThrow(SQLException.class);

        task.prepare(null); // the message will be resolved by IOC
        try {
            task.execute();
            fail();
        } catch (TaskExecutionException e) {
            // pass
        }

        verify(connection).compileQuery(any(QueryStatement.class));
        // implementation details of PostgresConnection
        // verify(statement).setLong(eq(1), eq(123L));
        verify(statement).execute();
        verify(resultSet).next();
        verify(connection).commit();
        verifyZeroInteractions(callback);
    }

}
