package info.smart_tools.smartactors.feature_management.feature_management_plugins;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_management.all_in_direcory_feature_tracker.AllInDirectoryFeatureTracker;
import info.smart_tools.smartactors.feature_management.download_feature_actor.DownloadFeatureActor;
import info.smart_tools.smartactors.feature_management.feature_manager_actor.FeatureManagerActor;
import info.smart_tools.smartactors.feature_management.load_feature_actor.LoadFeatureActor;
import info.smart_tools.smartactors.feature_management.unzip_feature_actor.UnzipFeatureActor;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;

import java.util.ArrayList;

/**
 * Created by sevenbits on 12/7/16.
 */
public class FeatureManagementStarterPlugin extends BootstrapPlugin {

    public FeatureManagementStarterPlugin(IBootstrap bootstrap) {
        super(bootstrap);
    }

    @Item("feature_management_starter")
    @After({"feature_management"})
    public void register()
            throws ResolutionException, RegistrationException, InvalidArgumentException, ChangeValueException, ChainNotFoundException {
//        IQueue<ITask> queue = IOC.resolve(Keys.getOrAdd("task_queue"));
//        Object chainId = IOC.resolve(
//                IOC.resolve(IOC.getKeyForKeyStorage(), "chain_id_from_map_name"), "loadCoreFeaturesAndFeatures"
//        );
//        IChainStorage chainStorage = IOC.resolve(
//                IOC.resolve(IOC.getKeyForKeyStorage(), IChainStorage.class.getCanonicalName())
//        );
//        IReceiverChain chain = chainStorage.resolve(chainId);
//
//        IMessageProcessingSequence processingSequence = IOC.resolve(
//                IOC.resolve(IOC.getKeyForKeyStorage(), IMessageProcessingSequence.class.getCanonicalName()),
//                5,
//                chain
//        );
//        IMessageProcessor mp = IOC.resolve(
//                IOC.resolve(IOC.getKeyForKeyStorage(), IMessageProcessor.class.getCanonicalName()),
//                queue,
//                processingSequence
//        );
//        IObject message = IOC.resolve(
//                IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.getCanonicalName())
//        );
//        IObject context = IOC.resolve(
//                IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.getCanonicalName())
//        );
//        mp.process(message, context);
    }
}
