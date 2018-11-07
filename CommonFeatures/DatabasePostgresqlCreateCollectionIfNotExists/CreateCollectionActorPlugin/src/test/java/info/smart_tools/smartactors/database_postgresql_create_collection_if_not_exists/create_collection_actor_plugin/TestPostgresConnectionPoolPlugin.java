package info.smart_tools.smartactors.database_postgresql_create_collection_if_not_exists.create_collection_actor_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IPoorAction;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.base.interfaces.ipool.exception.PoolPutException;
import info.smart_tools.smartactors.base.interfaces.ipool.exception.PoolTakeException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;

public final class TestPostgresConnectionPoolPlugin extends BootstrapPlugin {
    protected TestPostgresConnectionPoolPlugin(IBootstrap bootstrap) {
        super(bootstrap);
    }

    @Item("TestPostgresConnectionPoolPlugin")
    @After({})
    @Before("")
    public void register() throws ResolutionException, InvalidArgumentException, RegistrationException {
        IOC.register(Keys.resolveByName("PostgresConnectionPool"), new ApplyFunctionToArgumentsStrategy(args -> new IPool() {
            @Override
            public Object take() throws PoolTakeException {
                return null;
            }

            @Override
            public void put(Object o) throws PoolPutException {

            }

            @Override
            public void onAvailable(IPoorAction iPoorAction) {

            }
        }));
    }
}
