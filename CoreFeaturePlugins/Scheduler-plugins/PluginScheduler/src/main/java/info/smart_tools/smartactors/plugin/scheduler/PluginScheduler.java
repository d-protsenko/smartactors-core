package info.smart_tools.smartactors.plugin.scheduler;

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
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntryStorage;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntryStorageObserver;

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

    /**
     * Register scheduler entry storage creation strategy.
     *
     * @throws ResolutionException if error occurs resolving key
     * @throws RegistrationException if error occurs registering strategy
     * @throws InvalidArgumentException if {@link ApplyFunctionToArgumentsStrategy} does not like our function
     */
    @Item("scheduler_entry_storage")
    @After({"scheduler_entry_strategies"})
    public void registerStorage()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(Keys.getOrAdd(ISchedulerEntryStorage.class.getCanonicalName()), new ApplyFunctionToArgumentsStrategy(args -> {
            try {
                ISchedulerEntryStorageObserver observer = (args.length > 2) ? (ISchedulerEntryStorageObserver) args[2] : null;
                return new EntryStorage((IPool) args[0], (String) args[1], observer);
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
