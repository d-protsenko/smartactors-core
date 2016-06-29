package info.smart_tools.smartactors.core.db_task.upsert.psql;

import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.db_storage.exceptions.StorageException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.PreparedQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_task.upsert.psql.wrapper.IUpsertQueryMessage;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.FieldName;
import info.smart_tools.smartactors.core.iobject.IFieldName;
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
import info.smart_tools.smartactors.core.sql_commons.JDBCCompiledQuery;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.support.membermodification.MemberMatcher.field;

@PrepareForTest(IOC.class)
@RunWith(PowerMockRunner.class)
public class PSQLUpsertTaskTest {

    private PSQLUpsertTask task;
    private JDBCCompiledQuery compiledQuery;

    @BeforeClass
    public static void before() throws ScopeProviderException {
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
    }

    @Before
    public void setUp()
        throws InvalidArgumentException, RegistrationException, ResolutionException, ReadValueException, ChangeValueException {

        compiledQuery = mock(JDBCCompiledQuery.class);
        String collectionName = "collection";
        IUpsertQueryMessage IUpsertQueryMessage = mock(IUpsertQueryMessage.class);
        when(IUpsertQueryMessage.getCollectionName()).thenReturn(collectionName);

        IOC.register(
            IOC.getKeyForKeyStorage(),
            new ResolveByNameIocStrategy(
                (a) -> {
                    try {
                        return new Key<IKey>((String) a[0]);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
        );
        IKey<DBInsertTask> keyDBInsertTask = Keys.getOrAdd(DBInsertTask.class.toString());
        IKey<IUpsertQueryMessage> keyUpsertMessage= Keys.getOrAdd(IUpsertQueryMessage.class.toString());
        IKey<QueryStatement> keyQueryStatement = Keys.getOrAdd(QueryStatement.class.toString());
        IKey<IFieldName> keyFieldName = Keys.getOrAdd(IFieldName.class.toString());
        IKey<CompiledQuery> keyCompiledQuery = Keys.getOrAdd(CompiledQuery.class.toString());
        IOC.register(
            keyDBInsertTask,
            new SingletonStrategy(mock(DBInsertTask.class))
        );
        IOC.register(
            keyUpsertMessage,
            new SingletonStrategy(IUpsertQueryMessage)
        );
        QueryStatement queryStatement = mock(QueryStatement.class);
        when(queryStatement.getBodyWriter()).thenReturn(new StringWriter());
        IOC.register(
            keyQueryStatement,
            new SingletonStrategy(queryStatement)
        );
        IOC.register(
            keyFieldName,
            new CreateNewInstanceStrategy(
                (arg) -> {
                    try {
                        return new FieldName(String.valueOf(arg[0]));
                    } catch (InvalidArgumentException ignored) {}
                    return null;
                }
            )
        );
        IOC.register(
            keyCompiledQuery,
            new CreateNewInstanceStrategy(
                (arg) -> {
                    try {
                        StorageConnection connection = (StorageConnection) arg[0];
                        return connection.compileQuery(new QueryStatement());
                    } catch (StorageException ignored) {}
                    return null;
                }
            )
        );

        task = new PSQLUpsertTask();
    }

    @Test
    public void ShouldPrepareInsertQuery_When_IdIsNull()
        throws Exception {

        IKey<String> keyString = Keys.getOrAdd(String.class.toString());
        IOC.register(
            keyString,
            new CreateNewInstanceStrategy(
                (arg) -> null)
        );

        IObject upsertMessage = mock(IObject.class);
        StorageConnection connection = mock(StorageConnection.class);
        when(connection.compileQuery(any(PreparedQuery.class))).thenReturn(compiledQuery);

        task.setStorageConnection(connection);
        task.prepare(upsertMessage);

        DBInsertTask dbInsertTask = (DBInsertTask) MemberModifier.field(PSQLUpsertTask.class, "dbInsertTask").get(task);

        verify(dbInsertTask).setStorageConnection(connection);
        verifyStatic();
        IOC.resolve(Keys.getOrAdd(CompiledQuery.class.toString()), connection, PSQLUpsertTask.class.toString().concat("insert"), null);
    }

    @Test
    public void ShouldPrepareUpdateQuery_When_IdIsGiven()
        throws Exception {

        IKey<String> keyString = Keys.getOrAdd(String.class.toString());
        IOC.register(
            keyString,
            new CreateNewInstanceStrategy(String::valueOf));

        IObject upsertMessage = mock(IObject.class);
        StorageConnection connection = mock(StorageConnection.class);
        when(connection.compileQuery(any(PreparedQuery.class))).thenReturn(compiledQuery);

        task.setStorageConnection(connection);
        task.prepare(upsertMessage);

        verifyStatic();
        IOC.resolve(Keys.getOrAdd(CompiledQuery.class.toString()), connection, PSQLUpsertTask.class.toString().concat("update"), null);
    }

    @Test
    public void ShouldExecuteUpdate_When_ModeIsSetToUpdate()
        throws Exception {

        StorageConnection connection = mock(StorageConnection.class);
        when(connection.compileQuery(any(PreparedQuery.class))).thenReturn(compiledQuery);

        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(compiledQuery.getPreparedStatement()).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        field(PSQLUpsertTask.class, "compiledQuery").set(task, compiledQuery);
        field(PSQLUpsertTask.class, "mode").set(task, "update");

        task.setStorageConnection(connection);
        task.execute();

        verify(preparedStatement).executeUpdate();
    }

    @Test
    public void ShouldExecuteUpdate_When_ModeIsSetToInsert()
        throws Exception {

        StorageConnection connection = mock(StorageConnection.class);
        when(connection.compileQuery(any(PreparedQuery.class))).thenReturn(compiledQuery);

        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(compiledQuery.getPreparedStatement()).thenReturn(preparedStatement);
        field(PSQLUpsertTask.class, "compiledQuery").set(task, compiledQuery);
        field(PSQLUpsertTask.class, "mode").set(task, "insert");
        IObject rawUpsertQuery = mock(IObject.class);
        field(PSQLUpsertTask.class, "rawUpsertQuery").set(task, rawUpsertQuery);
        IFieldName fieldName = mock(IFieldName.class);
        field(PSQLUpsertTask.class, "idFieldName").set(task, fieldName);
        ResultSet resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.first()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(123L);

        task.setStorageConnection(connection);
        task.execute();

        verify(preparedStatement).executeQuery();
        verify(rawUpsertQuery).setValue(eq(fieldName), eq(123L));
    }

    @Test(expected = TaskExecutionException.class)
    public void ShouldThrowException_When_NoDocumentsHaveBeenUpdated()
        throws ResolutionException, ReadValueException, ChangeValueException, StorageException, TaskSetConnectionException, TaskExecutionException, TaskPrepareException, IllegalAccessException {

        StorageConnection connection = mock(StorageConnection.class);
        when(connection.compileQuery(any(PreparedQuery.class))).thenReturn(compiledQuery);

        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(compiledQuery.getPreparedStatement()).thenReturn(preparedStatement);
        field(PSQLUpsertTask.class, "compiledQuery").set(task, compiledQuery);
        field(PSQLUpsertTask.class, "mode").set(task, "update");

        task.setStorageConnection(connection);
        task.execute();
    }

    @Test(expected = TaskExecutionException.class)
    public void ShouldThrowException_When_NoDocumentsHaveBeenInserted()
        throws ResolutionException, ReadValueException, ChangeValueException, StorageException, TaskSetConnectionException, TaskExecutionException, TaskPrepareException, IllegalAccessException {

        StorageConnection connection = mock(StorageConnection.class);
        when(connection.compileQuery(any(PreparedQuery.class))).thenReturn(compiledQuery);

        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(compiledQuery.getPreparedStatement()).thenReturn(preparedStatement);
        field(PSQLUpsertTask.class, "compiledQuery").set(task, compiledQuery);
        field(PSQLUpsertTask.class, "mode").set(task, "insert");

        task.setStorageConnection(connection);
        task.execute();
    }
}
