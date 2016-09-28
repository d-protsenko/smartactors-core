package info.smart_tools.smartactors.actors.create_session;

import info.smart_tools.smartactors.actors.create_session.wrapper.CreateSessionMessage;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.field.field.Field;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.database.interfaces.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.scope.iscope.IScope;

import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

//TODO :: add more tests
public class CreateSessionActorTest {
    CreateSessionActor actor;
    IPool connectionPool = mock(IPool.class);
    IDatabaseTask task = mock(IDatabaseTask.class);

    @Before
    public void setUp() throws Exception {
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
        IKey keyIField = Keys.getOrAdd(IField.class.getCanonicalName());
        IOC.register(keyIField, new ApplyFunctionToArgumentsStrategy(
                (args) -> {
                    String fieldName = String.valueOf(args[0]);
                    try {
                        return new Field(new FieldName(fieldName));
                    } catch (InvalidArgumentException e) {
                        throw new RuntimeException("Can't resolve IField: ", e);
                    }
                }
        ));
        IKey keyIObject = Keys.getOrAdd(IObject.class.getCanonicalName());
        IOC.register(keyIObject, new ApplyFunctionToArgumentsStrategy(
                (args) -> {
                    try {
                        if (args.length == 0) {
                            return new DSObject();
                        } else {
                            return new DSObject((String) args[0]);
                        }
                    } catch (InvalidArgumentException e) {
                        throw new RuntimeException("Can't resolve IObject: ", e);
                    }
                }
        ));
        IKey keyConnectionOptions = Keys.getOrAdd("PostgresConnectionOptions");
        IOC.register(keyConnectionOptions, new ApplyFunctionToArgumentsStrategy(
                (args) -> mock(ConnectionOptions.class)
        ));
        IKey keyConnectionPool = Keys.getOrAdd("PostgresConnectionPool");
        IOC.register(keyConnectionPool, new ApplyFunctionToArgumentsStrategy(
                (args) -> connectionPool
        ));
        IKey keyDBTask = Keys.getOrAdd("db.collection.search");
        IOC.register(keyDBTask, new ApplyFunctionToArgumentsStrategy(
                (args) -> task
        ));
        IKey keyNextId = Keys.getOrAdd("db.collection.nextid");
        IOC.register(keyNextId, new ApplyFunctionToArgumentsStrategy(
                (args) -> "123"
        ));

        IObject params = new DSObject("{ \"collectionName\": \"session\" }");
        actor = new CreateSessionActor(params);
    }

    @Test
    public void Should_insertNewSessionInMessage_When_SessionIdIsNull() throws Exception {
        CreateSessionMessage inputMessage = mock(CreateSessionMessage.class);
        when(inputMessage.getSessionId()).thenReturn(null);
        actor.resolveSession(inputMessage);
        verify(inputMessage).setSession(any(IObject.class));
    }
}

