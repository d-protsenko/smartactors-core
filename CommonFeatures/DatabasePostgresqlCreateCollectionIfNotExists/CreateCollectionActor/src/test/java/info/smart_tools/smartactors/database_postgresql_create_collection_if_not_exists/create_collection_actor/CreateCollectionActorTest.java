package info.smart_tools.smartactors.database_postgresql_create_collection_if_not_exists.create_collection_actor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.database_postgresql_create_collection_if_not_exists.create_collection_actor.wrapper.CreateCollectionWrapper;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.ioc.string_ioc_key.Key;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.verification.VerificationModeFactory;

import static org.mockito.Mockito.*;

public class CreateCollectionActorTest {
    private CreateCollectionActor actor = new CreateCollectionActor();
    private IStrategyContainer container = new StrategyContainer();

    @Before
    public void init() throws Exception {
        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope scope = ScopeProvider.getScope(keyOfMainScope);
        scope.setValue(IOC.getIocKey(), container);
        ScopeProvider.setCurrentScope(scope);
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
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            try {
                                return new FieldName((String) args[0]);
                            } catch (Exception e) {
                                throw new RuntimeException("exception", e);
                            }
                        }
                )
        );
        IOC.register(Keys.getOrAdd(IObject.class.getCanonicalName()),
                new ApplyFunctionToArgumentsStrategy(args -> {
                    if (args.length == 0) {
                        return new DSObject();
                    } else if (args.length == 1 && args[0] instanceof String) {
                        try {
                            return new DSObject((String) args[0]);
                        } catch (InvalidArgumentException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        throw new RuntimeException("Invalid arguments for IObject creation.");
                    }
                })
        );
        IOC.register(Keys.getOrAdd("connectionOptions"),
                new ApplyFunctionToArgumentsStrategy(args -> mock(ConnectionOptions.class))
        );
        IOC.register(Keys.getOrAdd("PostgresConnectionPool"),
                new ApplyFunctionToArgumentsStrategy(args -> mock(IPool.class))
        );
    }

    @Test
    public void Should_CreateCollection() throws Exception {
        CreateCollectionWrapper wrapper = mock(CreateCollectionWrapper.class);
        when(wrapper.getCollectionName()).thenReturn("test");
        when(wrapper.getConnectionOptionsRegistrationName()).thenReturn("connectionOptions");
        when(wrapper.getOptions()).thenReturn(null);

        ITask task = mock(ITask.class);
        IOC.register(Keys.getOrAdd("db.collection.create-if-not-exists"),
                new ApplyFunctionToArgumentsStrategy(args -> task)
        );
        actor.createTable(wrapper);

        verify(task, VerificationModeFactory.times(1)).execute();
    }
}
