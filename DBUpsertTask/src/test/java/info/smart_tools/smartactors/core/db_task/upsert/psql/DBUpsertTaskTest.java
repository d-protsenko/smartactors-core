package info.smart_tools.smartactors.core.db_task.upsert.psql;

import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.db_storage.exceptions.StorageException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.PreparedQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.ConnectionPool;
import info.smart_tools.smartactors.core.db_task.upsert.psql.exception.DBUpsertTaskException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
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
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class DBUpsertTaskTest {

    private DBUpsertTask task;
    private CompiledQuery compiledQuery;
    private ConnectionPool connectionPool;
    private String collectionName;

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
        throws DBUpsertTaskException, InvalidArgumentException, RegistrationException, ResolutionException {

        compiledQuery = mock(CompiledQuery.class);
        connectionPool = mock(ConnectionPool.class);
        collectionName = "collection";

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
        IKey<DBInsertTask> keyDBInsertTask = IOC.resolve(IOC.getKeyForKeyStorage(), DBInsertTask.class.toString());
        IKey<QueryStatement> keyQueryStatement = IOC.resolve(IOC.getKeyForKeyStorage(), QueryStatement.class.toString());
        IKey<IFieldName> keyFieldName = IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.toString());
        IKey<String> keyString = IOC.resolve(IOC.getKeyForKeyStorage(), String.class.toString());
        IOC.register(
            keyDBInsertTask,
            new CreateNewInstanceStrategy(
                (args) -> new DBInsertTask((ConnectionPool) args[0], String.valueOf(args[1]))
            )
        );
        IOC.register(
            keyQueryStatement,
            new CreateNewInstanceStrategy(
                (arg) -> new QueryStatement()
            )
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
            keyString,
            new CreateNewInstanceStrategy(
                String::valueOf
            )
        );

        task = new DBUpsertTask(connectionPool, collectionName);
    }

    @Test
    public void ShouldPrepareQuery() throws TaskPrepareException, ResolutionException, ReadValueException, ChangeValueException, StorageException {

        IObject upsertMessage = mock(IObject.class);
        StorageConnection connection = mock(StorageConnection.class);
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.compileQuery(any(PreparedQuery.class))).thenReturn(compiledQuery);

        task.prepare(upsertMessage);
    }
}
