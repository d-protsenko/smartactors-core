package info.smart_tools.smartactors.version_management_plugins.version_management_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.FunctionExecutionException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.non_blocking_queue.NonBlockingQueue;
import info.smart_tools.smartactors.version_management.chain_version_manager.ChainIdFromMapNameStrategy;
import info.smart_tools.smartactors.version_management.versioned_task_queue_decorator.VersionedTaskQueueDecorator;

import java.util.concurrent.ConcurrentLinkedQueue;

public class VersionManagementPlugin  extends BootstrapPlugin {

    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public VersionManagementPlugin(final IBootstrap bootstrap) {
        super(bootstrap);
    }

    @Item("versioned_chain_id_from_map_name_strategy")
    @After({"messaging_identifiers"})
    @Before({"config_section:maps"})
    public void registerChainIdFromMapNameStrategy()
            throws ResolutionException, RegistrationException, InvalidArgumentException {

        ChainIdFromMapNameStrategy strategy = new ChainIdFromMapNameStrategy();

        IOC.register(Keys.getOrAdd("chain_id_from_map_name_and_message"), strategy.getResolveByMessageStrategy());
        IOC.register(Keys.getOrAdd("chain_id_from_map_name"), strategy.getResolveByModuleDependenciesStrategy());
        IOC.register(Keys.getOrAdd("register_message_version_strategy"), strategy.getRegisterMessageVersionStrategy());
    }

    @Item("versioned_task_queue")
    @After({"queue"})
    @Before({"config_section:objects"})
    public void registerVersionedTaskQueueStrategy()
            throws ResolutionException, RegistrationException, InvalidArgumentException {

        IOC.register(Keys.getOrAdd(IQueue.class.getCanonicalName()), new ApplyFunctionToArgumentsStrategy(args -> {
            try {
                return new VersionedTaskQueueDecorator(new NonBlockingQueue<>(new ConcurrentLinkedQueue<>()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }

    @ItemRevert("versioned_chain_id_from_map_name_strategy")
    public void unregisterChainIdFromMapNameStrategy()
            throws ResolutionException, RegistrationException, InvalidArgumentException {

        String itemName = "versioned_chain_id_from_map_name_strategy";
        String keyName;

        keyName = "register_message_version_strategy";
        try {
            IOC.remove(Keys.getOrAdd(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregitration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }

        keyName = "chain_id_from_map_name";
        try {
            IOC.remove(Keys.getOrAdd(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregitration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }

        keyName = "chain_id_from_map_name_and_message";
        try {
            IOC.remove(Keys.getOrAdd(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregitration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }
    }

    @ItemRevert("versioned_task_queue")
    public void unregisterVersionedTaskQueueStrategy()
            throws ResolutionException, RegistrationException, InvalidArgumentException {

        String itemName = "versioned_task_queue";
        String keyName;

        keyName = IQueue.class.getCanonicalName();
        try {
            IOC.remove(Keys.getOrAdd(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregitration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }
    }
}