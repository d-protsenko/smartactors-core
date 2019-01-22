package info.smart_tools.smartactors.shutdown_plugins.shutdown_task_processing_strategies_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IFunctionTwoArgs;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecutionException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.FunctionExecutionException;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.iup_counter.IUpCounter;
import info.smart_tools.smartactors.base.iup_counter.exception.UpCounterCallbackExecutionException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.base.strategy.strategy_storage_with_cache_strategy.StrategyStorageWithCacheStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.shutdown.shutdown_task_processing_strategies.*;
import info.smart_tools.smartactors.task.interfaces.itask_dispatcher.ITaskDispatcher;
import info.smart_tools.smartactors.task.itask_preprocess_strategy.ITaskProcessStrategy;

import java.util.Map;

public class ShutdownTaskProcessingStrategiesPlugin extends BootstrapPlugin {
    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public ShutdownTaskProcessingStrategiesPlugin(final IBootstrap bootstrap) {
            super(bootstrap);
    }

    @Item("shutdown_task_process_strategies")
    @After({"IOC"})
    public void registerStrategies()
            throws ResolutionException, InvalidArgumentException, RegistrationException {
        IOC.register(Keys.resolveByName("ignore task processing strategy"),
                new SingletonStrategy(new IgnoreTaskStrategy()));
        IOC.register(Keys.resolveByName("execute task processing strategy"),
                new SingletonStrategy(new ExecuteTaskStrategy()));
        IOC.register(Keys.resolveByName("notify task processing strategy"),
                new SingletonStrategy(new NotifyTaskStrategy()));
        IOC.register(Keys.resolveByName("limit trials task processing strategy"),
                new ApplyFunctionToArgumentsStrategy(args -> {
                    int maxTrials = (int) args[0];
                    int silentTrials = maxTrials;

                    if (args.length > 1) {
                        silentTrials = (int) args[1];
                    }

                    try {
                        return new LimitTrialCountStrategy(maxTrials, silentTrials);
                    } catch (InvalidArgumentException e) {
                        throw new FunctionExecutionException(e);
                    }
                }));

        IResolutionStrategy strategyStorage = new StrategyStorageWithCacheStrategy(
                a -> a,
                (IFunctionTwoArgs<Map<Class, Object>, Class, Object>) (map, clz) -> {
                    for (Map.Entry<Class, Object> entry : map.entrySet()) {
                        if (clz.isAssignableFrom(entry.getKey())) {
                            return entry.getValue();
                        }
                    }

                    return null;
                }
        );

        IOC.register(Keys.resolveByName("shutdown mode task processing strategy by task class"),
                strategyStorage);
        IOC.register(Keys.resolveByName("expandable_strategy#shutdown mode task processing strategy by task class"),
                new SingletonStrategy(strategyStorage));

        IOC.register(Keys.resolveByName("task processing strategy for shutdown mode"),
                new SingletonStrategy(new CompositeStrategy(
                        Keys.resolveByName("shutdown mode task processing strategy by task class"),
                        IOC.resolve(Keys.resolveByName("execute task processing strategy")))));
    }

    @ItemRevert("shutdown_task_process_strategies")
    public void unregisterStrategies() {
        String itemName = "shutdown_task_process_strategies";
        String keyName = "";

        try {
            keyName = "task processing strategy for shutdown mode";
            IOC.remove(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }

        try {
            keyName = "expandable_strategy#shutdown mode task processing strategy by task class";
            IOC.remove(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }

        try {
            keyName = "shutdown mode task processing strategy by task class";
            IOC.remove(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }

        try {
            keyName = "limit trials task processing strategy";
            IOC.remove(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }

        try {
            keyName = "notify task processing strategy";
            IOC.remove(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }

        try {
            keyName = "execute task processing strategy";
            IOC.remove(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }

        try {
            keyName = "ignore task processing strategy";
            IOC.remove(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }
    }

    @Item("task_dispatcher_pre_shutdown_mode_callbacks")
    @After({
        "shutdown_task_process_strategies",
        "root_upcounter",
    })
    public void registerUpcounterCallbackForTaskDispatcherShutdown()
            throws ResolutionException, UpCounterCallbackExecutionException {
        IUpCounter upCounter = IOC.resolve(Keys.resolveByName("root upcounter"));

        upCounter.onShutdownRequest(this.toString(), mode -> {
            try {
                ITaskDispatcher taskDispatcher = IOC.resolve(Keys.resolveByName("task_dispatcher"));
                ITaskProcessStrategy taskProcessStrategy = IOC.resolve(Keys.resolveByName("task processing strategy for shutdown mode"));
                taskDispatcher.setProcessStrategy(taskProcessStrategy);
            } catch (ResolutionException e) {
                throw new ActionExecutionException(e);
            }
        });
    }

    @ItemRevert("task_dispatcher_pre_shutdown_mode_callbacks")
    public void unregisterUpcounterCallbackForTaskDispatcherShutdown() {
    }
}
