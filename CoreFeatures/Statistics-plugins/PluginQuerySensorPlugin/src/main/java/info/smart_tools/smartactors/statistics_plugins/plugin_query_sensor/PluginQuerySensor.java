package info.smart_tools.smartactors.statistics_plugins.plugin_query_sensor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntryStorage;
import info.smart_tools.smartactors.statistics.sensors.scheduled_query_sensor.QuerySensorCreationStrategy;
import info.smart_tools.smartactors.statistics.sensors.scheduled_query_sensor.QuerySensorSchedulerAction;

/**
 *
 */
public class PluginQuerySensor extends BootstrapPlugin {

    /**
     * The constructor.
     *
     * @param bootstrap the bootstrap
     */
    public PluginQuerySensor(final IBootstrap bootstrap) {
        super(bootstrap);
    }

    /**
     * Register the strategy creating a query sensor.
     *
     * @throws ResolutionException if error occurs resolving key or dependencies of strategy
     * @throws RegistrationException if error occurs registering the strategy
     */
    @Item("query_senor_creation_strategy")
    @After({"query_sensor_scheduler_storage", "query_sensor_scheduler_action"})
    public void registerQuerySensorStrategy()
            throws ResolutionException, RegistrationException {
        IOC.register(Keys.getOrAdd("create query sensor"), new QuerySensorCreationStrategy());
    }

    /**
     * Create and register the scheduler entries storage for use by query sensors.
     *
     * @throws ResolutionException if error occurs resolving key or creating the storage
     * @throws RegistrationException if error occurs registering the storage
     * @throws InvalidArgumentException if the storage is not accepted by {@link SingletonStrategy}
     */
    @Item("query_sensor_scheduler_storage")
    @After({"scheduler_entry_storage"})
    public void createSchedulerStorage()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        ISchedulerEntryStorage entryStorage = IOC.resolve(Keys.getOrAdd("local only scheduler entry storage"));
        IOC.register(Keys.getOrAdd("query sensors scheduler storage"),
                new SingletonStrategy(entryStorage));
    }

    /**
     * Register the scheduler action for query sensor entries.
     *
     * @throws RegistrationException if error occurs registering the action
     * @throws ResolutionException if error occurs resolving the key or dependencies of the action
     * @throws InvalidArgumentException if action is not accepted by the {@link SingletonStrategy}
     */
    @Item("query_sensor_scheduler_action")
    public void registerSchedulerAction()
            throws RegistrationException, ResolutionException, InvalidArgumentException {
        IOC.register(Keys.getOrAdd("query sensor scheduler action"), new SingletonStrategy(new QuerySensorSchedulerAction()));
    }
}