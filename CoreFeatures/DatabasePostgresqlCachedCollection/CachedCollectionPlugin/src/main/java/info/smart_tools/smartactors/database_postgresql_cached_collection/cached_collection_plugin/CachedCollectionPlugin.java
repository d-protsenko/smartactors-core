package info.smart_tools.smartactors.database_postgresql_cached_collection.cached_collection_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.database_postgresql_cached_collection.cached_collection_actor.CachedCollectionActor;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

public class CachedCollectionPlugin extends BootstrapPlugin {

     /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public CachedCollectionPlugin(final IBootstrap bootstrap) {
            super(bootstrap);
    }

    @Item("PostgresCachedCollectionPlugin")
    @After({})
    @Before("")
    public void doSomeThing() throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(
                Keys.getOrAdd("PostgresCachedCollectionActor"),
                new ApplyFunctionToArgumentsStrategy(args -> new CachedCollectionActor())
        );
    }
}
