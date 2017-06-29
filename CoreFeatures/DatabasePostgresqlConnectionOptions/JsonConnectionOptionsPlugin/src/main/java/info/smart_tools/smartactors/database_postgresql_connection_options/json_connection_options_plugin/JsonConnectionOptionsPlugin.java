package info.smart_tools.smartactors.database_postgresql_connection_options.json_connection_options_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.database_postgresql_connection_options.json_connection_options_actor.JsonConnectionOptionsActor;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

public class JsonConnectionOptionsPlugin extends BootstrapPlugin {

    /**
     * The constructor.
     *
     * @param bootstrap the bootstrap
     */
    public JsonConnectionOptionsPlugin(final IBootstrap bootstrap) {
        super(bootstrap);
    }

    @Item("JsonConnectionOptionsPlugin")
    @After({})
    @Before("")
    public void registerActor() throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(
                Keys.getOrAdd("PostgresJsonConnectionOptionsActor"),
                new ApplyFunctionToArgumentsStrategy(args -> new JsonConnectionOptionsActor())
        );
    }
}
