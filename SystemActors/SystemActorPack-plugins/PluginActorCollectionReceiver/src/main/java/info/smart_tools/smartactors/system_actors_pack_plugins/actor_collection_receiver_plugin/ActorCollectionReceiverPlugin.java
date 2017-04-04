package info.smart_tools.smartactors.system_actors_pack_plugins.actor_collection_receiver_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.FunctionExecutionException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.system_actors_pack.actor_collection_receiver.ActorCollectionReceiver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plugin registers into IOC strategy for creation new instance of {@link ActorCollectionReceiver}.
 */
public class ActorCollectionReceiverPlugin extends BootstrapPlugin {

    /**
     * The constructor.
     *
     * @param bootstrap the bootstrap
     */
    public ActorCollectionReceiverPlugin(final IBootstrap bootstrap) {
        super(bootstrap);
    }

    @Item("ActorCollectionReceiver")
    public void registerCollectionReceiver()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(
                Keys.getOrAdd("ActorCollection"),
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            try {
                                Map<Object, IMessageReceiver> childStorage = new ConcurrentHashMap<>();
                                return new ActorCollectionReceiver(childStorage);
                            } catch (Exception e) {
                                throw new FunctionExecutionException("Could not create new instance of ActorCollectionReceiver.", e);
                            }
                        }
                )
        );
    }
}
