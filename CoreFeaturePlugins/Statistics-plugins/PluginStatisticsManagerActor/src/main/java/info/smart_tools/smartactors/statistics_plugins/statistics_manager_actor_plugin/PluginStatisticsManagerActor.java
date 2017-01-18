package info.smart_tools.smartactors.statistics_plugins.statistics_manager_actor_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.FunctionExecutionException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.statistics.statistics_manager.StatisticsManagerActor;

/**
 *
 */
public class PluginStatisticsManagerActor extends BootstrapPlugin {
    /**
     * The constructor.
     *
     * @param bootstrap the bootstrap
     */
    public PluginStatisticsManagerActor(final IBootstrap bootstrap) {
        super(bootstrap);
    }

    /**
     * Register strategy for creation of new instance of statistics manager actor.
     *
     * @throws ResolutionException if error occurs resolving the key
     * @throws RegistrationException if error occurs registering the strategy
     * @throws InvalidArgumentException if strategy does not accept the function
     */
    @Item("statistics_manager_actor")
    public void registerActorCreationDependency()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(Keys.getOrAdd("statistics manager actor"), new ApplyFunctionToArgumentsStrategy(args -> {
            try {
                return new StatisticsManagerActor();
            } catch (ResolutionException e) {
                throw new FunctionExecutionException(e);
            }
        }));
    }
}
