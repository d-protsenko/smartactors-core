package info.smart_tools.smartactors.actors.create_session;

import info.smart_tools.smartactors.actors.create_session.wrapper.CreateSessionMessage;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.field.Field;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.iscope.IScope;

import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
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
