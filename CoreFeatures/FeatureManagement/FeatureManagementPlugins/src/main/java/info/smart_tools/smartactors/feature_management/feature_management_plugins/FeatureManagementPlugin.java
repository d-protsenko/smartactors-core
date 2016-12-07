package info.smart_tools.smartactors.feature_management.feature_management_plugins;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
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
public class FeatureManagementPlugin extends BootstrapPlugin {

    public FeatureManagementPlugin(IBootstrap bootstrap) {
        super(bootstrap);
    }

    @Item("feature_management")
    @After({"IOC", "configuration_manager", "config_sections:done", "IFieldNamePlugin", "ConfigurationObject"})
    public void register()
            throws ResolutionException, RegistrationException, InvalidArgumentException, ChainNotFoundException, ChangeValueException {

        IOC.register(Keys.getOrAdd("feature-repositories"), new SingletonStrategy(
                new ArrayList<IObject>()
        ));

        IOC.register(Keys.getOrAdd("FeatureManager"), new ApplyFunctionToArgumentsStrategy(
                (args) -> {
                    try {
                        return new FeatureManagerActor();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
        IOC.register(Keys.getOrAdd("DownloadFeatureActor"), new ApplyFunctionToArgumentsStrategy(
                (args) -> {
                    try {
                        return new DownloadFeatureActor();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
        IOC.register(Keys.getOrAdd("UnzipFeatureActor"), new ApplyFunctionToArgumentsStrategy(
                (args) -> {
                    try {
                        return new UnzipFeatureActor();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
        IOC.register(Keys.getOrAdd("LoadFeatureActor"), new ApplyFunctionToArgumentsStrategy(
                (args) -> {
                    try {
                        return new LoadFeatureActor();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
        IOC.register(Keys.getOrAdd("AllInDirectoryFeatureTracker"), new ApplyFunctionToArgumentsStrategy(
                (args) -> {
                    try {
                        return new AllInDirectoryFeatureTracker();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
    }
}
