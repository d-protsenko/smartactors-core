package info.smart_tools.smartactors.system_actors_pack_plugins.actor_collection_receiver_plugin;

import info.smart_tools.smartactors.system_actors_pack.actor_collection_receiver.ActorCollectionReceiver;
import info.smart_tools.smartactors.system_actors_pack.actor_collection_receiver.ActorCollectionRouter;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.message_processing_interfaces.irouter.IRouter;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;

/**
 * Implementation of {@link IPlugin}.
 * Plugin registers into IOC strategy for creation new instance of {@link ActorCollectionReceiver}.
 */
public class ActorCollectionReceiverPlugin implements IPlugin {

    /** Local storage for instance of {@link IBootstrap}*/
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * @param bootstrap Target bootstrap for adding strategy
     * @throws InvalidArgumentException if any errors occurred
     */
    public ActorCollectionReceiverPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap)
            throws InvalidArgumentException {
        if (null == bootstrap) {
            throw new InvalidArgumentException("Incoming argument should not be null.");
        }
        this.bootstrap = bootstrap;
    }

    @Override
    public void load()
            throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("ActorCollectionReceiver");
            item
//                    .after("IOC")
//                    .before("configure")
//                    .before("starter")
//                    .after("IFieldPlugin")
//                    .after("IFieldNamePlugin")
                    .process(
                            () -> {
                                try {
                                    IOC.register(
                                            IOC.resolve(
                                                    IOC.getKeyForKeyStorage(),
                                                    "ActorCollection"
                                            ),
                                            new ApplyFunctionToArgumentsStrategy(
                                                    (args) -> {
                                                        try {
                                                            IRouter router = new ActorCollectionRouter();
                                                            return new ActorCollectionReceiver(router);
                                                        } catch (Exception e) {
                                                            throw new RuntimeException(
                                                                    "Could not create new instance of ActorCollectionReceiver."
                                                                    , e
                                                            );
                                                        }
                                                    }
                                            )
                                    );
                                } catch (ResolutionException e) {
                                    throw new ActionExecuteException("ActorCollectionReceiver plugin can't load: can't get ActorCollectionReceiver key", e);
                                } catch (InvalidArgumentException e) {
                                    throw new ActionExecuteException("ActorCollectionReceiver plugin can't load: can't create strategy", e);
                                } catch (RegistrationException e) {
                                    throw new ActionExecuteException("ActorCollectionReceiver plugin can't load: can't register new strategy", e);
                                }
                            }
                    );
            this.bootstrap.add(item);
        } catch (Throwable e) {
            throw new PluginException("Could not load 'ActorReceiverCreator plugin'", e);
        }
    }
}
