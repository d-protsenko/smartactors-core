package info.smart_tools.smartactors.plugin.actor_collection_receiver;

import info.smart_tools.smartactors.core.actor_collection_receiver.ActorCollectionReceiver;
import info.smart_tools.smartactors.core.actor_collection_receiver.ActorCollectionRouter;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.irouter.IRouter;
import info.smart_tools.smartactors.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;

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
                    .after("IOC")
                    .before("configure")
                    .after("IFieldPlugin")
                    .after("IFieldNamePlugin")
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
