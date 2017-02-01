package info.smart_tools.smartactors.scheduler_plugins.scheduler_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.FunctionExecutionException;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.scheduler.actor.SchedulerActor;
import info.smart_tools.smartactors.scheduler.actor.impl.DefaultSchedulerAction;
import info.smart_tools.smartactors.scheduler.actor.impl.EntryImpl;
import info.smart_tools.smartactors.scheduler.actor.impl.EntryStorage;
import info.smart_tools.smartactors.scheduler.actor.impl.EntryStorageRefresher;
import info.smart_tools.smartactors.scheduler.actor.impl.remote_storage.DatabaseRemoteStorage;
import info.smart_tools.smartactors.scheduler.actor.impl.remote_storage.IRemoteEntryStorage;
import info.smart_tools.smartactors.scheduler.actor.impl.remote_storage.NullRemoteStorage;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntryStorage;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntryStorageObserver;
import info.smart_tools.smartactors.timer.interfaces.itimer.exceptions.TaskScheduleException;

/**
 * Plugin that registers scheduler actor and related components.
 */
public class PluginScheduler extends BootstrapPlugin {
    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public PluginScheduler(final IBootstrap bootstrap) {
        super(bootstrap);
    }

    /**
     * Register default action to be executed by scheduler when entry fires.
     *
     * @throws ResolutionException if error occurs resolving key
     * @throws RegistrationException if error occurs registering strategy
     * @throws InvalidArgumentException if {@link ApplyFunctionToArgumentsStrategy} does not like our function
     */
    @Item("scheduler_default_action")
    public void registerDefaultAction()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(
                Keys.getOrAdd("default scheduler action"),
                new SingletonStrategy(new DefaultSchedulerAction())
        );
    }

    /**
     * Register scheduler entry creation and restoration strategies.
     *
     * @throws ResolutionException if error occurs resolving key
     * @throws RegistrationException if error occurs registering strategy
     * @throws InvalidArgumentException if {@link ApplyFunctionToArgumentsStrategy} does not like our function
     */
    @Item("scheduler_entry_strategies")
    public void registerEntry()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(
                Keys.getOrAdd("new scheduler entry"),
                new ApplyFunctionToArgumentsStrategy(args -> {
                    try {
                        return EntryImpl.newEntry((IObject) args[0], (ISchedulerEntryStorage) args[1]);
                    } catch (Exception e) {
                        throw new FunctionExecutionException(e);
                    }
                })
        );
        IOC.register(
                Keys.getOrAdd("restore scheduler entry"),
                new ApplyFunctionToArgumentsStrategy(args -> {
                    try {
                        return EntryImpl.restoreEntry((IObject) args[0], (ISchedulerEntryStorage) args[1]);
                    } catch (Exception e) {
                        throw new FunctionExecutionException(e);
                    }
                })
        );
    }

    private static final long DEFAULT_BASE_REFRESH_INTERVAL = 1000 * 60; // 60 seconds
    private static final long DEFAULT_REFRESH_PAGE_SIZE = 1000 * 60;

    /**
     * Register default refresh parameters resolution strategies.
     *
     * <p>
     * By-default constant refresh intervals and page size are set but the strategies may be replaced.
     * </p>
     *
     * @throws ResolutionException if error occurs resolving a key
     * @throws RegistrationException if error occurs registering a strategy
     * @throws InvalidArgumentException if {@link SingletonStrategy} does not accept default values
     */
    @Item("scheduler_storage_refresh_parameters:default")
    public void registerDefaultRefreshIntervals()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        // RRI = 60 sec
        IOC.register(Keys.getOrAdd("scheduler storage refresh interval: rri"),
                new SingletonStrategy(DEFAULT_BASE_REFRESH_INTERVAL));
        // RAI = 90 sec
        IOC.register(Keys.getOrAdd("scheduler storage refresh interval: rai"),
                new SingletonStrategy(DEFAULT_BASE_REFRESH_INTERVAL + DEFAULT_BASE_REFRESH_INTERVAL / 2));
        // RSI = 120 sec
        IOC.register(Keys.getOrAdd("scheduler storage refresh interval: rsi"),
                new SingletonStrategy(DEFAULT_BASE_REFRESH_INTERVAL * 2));

        IOC.register(Keys.getOrAdd("scheduler storage refresh page size"),
                new SingletonStrategy(DEFAULT_REFRESH_PAGE_SIZE));
    }

    /**
     * Register scheduler entry storage creation strategy.
     *
     * @throws ResolutionException if error occurs resolving key
     * @throws RegistrationException if error occurs registering strategy
     * @throws InvalidArgumentException if {@link ApplyFunctionToArgumentsStrategy} does not like our function
     */
    @Item("scheduler_entry_storage")
    @After({"scheduler_entry_strategies", "scheduler_storage_refresh_parameters:default"})
    public void registerStorage()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(Keys.getOrAdd(ISchedulerEntryStorage.class.getCanonicalName()), new ApplyFunctionToArgumentsStrategy(args -> {
            try {
                ISchedulerEntryStorageObserver observer = (args.length > 2) ? (ISchedulerEntryStorageObserver) args[2] : null;
                IRemoteEntryStorage remoteEntryStorage = new DatabaseRemoteStorage((IPool) args[0], (String) args[1]);
                EntryStorage storage = new EntryStorage(remoteEntryStorage, observer);
                long rrInterval = IOC.resolve(Keys.getOrAdd("scheduler storage refresh interval: rri"), args);
                long raInterval = IOC.resolve(Keys.getOrAdd("scheduler storage refresh interval: rai"), args);
                long rsInterval = IOC.resolve(Keys.getOrAdd("scheduler storage refresh interval: rsi"), args);
                int refreshPageSize = IOC.resolve(Keys.getOrAdd("scheduler storage refresh page size"), args);
                new EntryStorageRefresher(storage, remoteEntryStorage, rrInterval, raInterval, rsInterval, refreshPageSize);
                return storage;
            } catch (ResolutionException | TaskScheduleException e) {
                throw new FunctionExecutionException(e);
            }
        }));
        IOC.register(Keys.getOrAdd("local only scheduler entry storage"), new ApplyFunctionToArgumentsStrategy(args -> {
            try {
                ISchedulerEntryStorageObserver observer = (args.length > 0) ? (ISchedulerEntryStorageObserver) args[0] : null;
                return new EntryStorage(NullRemoteStorage.INSTANCE,  observer);
            } catch (ResolutionException e) {
                throw new FunctionExecutionException(e);
            }
        }));
    }

    /**
     * Register scheduler actor creation strategy.
     *
     * @throws ResolutionException if error occurs resolving key
     * @throws RegistrationException if error occurs registering strategy
     * @throws InvalidArgumentException if {@link ApplyFunctionToArgumentsStrategy} does not like our function
     */
    @Item("scheduler_actor")
    @After({"scheduler_entry_storage", "scheduler_entry_strategies"})
    public void registerActor()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(Keys.getOrAdd("scheduler actor"), new ApplyFunctionToArgumentsStrategy(args -> {
            try {
                return new SchedulerActor((IObject) args[0]);
            } catch (Exception e) {
                throw new FunctionExecutionException(e);
            }
        }));
    }
}
