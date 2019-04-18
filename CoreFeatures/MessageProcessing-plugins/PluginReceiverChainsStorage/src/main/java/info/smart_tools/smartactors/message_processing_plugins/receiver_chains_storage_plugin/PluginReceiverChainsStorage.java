package info.smart_tools.smartactors.message_processing_plugins.receiver_chains_storage_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.FunctionExecutionException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.message_processing.chain_storage.ChainStorage;
import info.smart_tools.smartactors.message_processing.chain_storage.impl.ChainStateImpl;
import info.smart_tools.smartactors.message_processing.chain_storage.interfaces.IChainState;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.irouter.IRouter;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class PluginReceiverChainsStorage implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public PluginReceiverChainsStorage(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            BootstrapItem chainsStorageChainStateItem = new BootstrapItem("receiver_chains_storage_chain_state");

            chainsStorageChainStateItem.after("IOC");

            chainsStorageChainStateItem
                    .process(() -> {
                        try {
                            IOC.register(
                                    Keys.resolveByName(IChainState.class.getCanonicalName()),
                                    new ApplyFunctionToArgumentsStrategy(args -> {
                                        try {
                                            return new ChainStateImpl((IReceiverChain) args[0]);
                                        } catch (ResolutionException | InvalidArgumentException e) {
                                            throw new FunctionExecutionException(e);
                                        }
                                    })
                            );
                        } catch (ResolutionException | RegistrationException | InvalidArgumentException e) {
                            throw new ActionExecuteException(e);
                        }
                    })
                    .revertProcess(() -> {
                        String itemName = "receiver_chains_storage_chain_state";
                        String keyName = "";

                        try {
                            keyName = IChainState.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Unregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                    });

            bootstrap.add(chainsStorageChainStateItem);

            BootstrapItem chainsStorageItem = new BootstrapItem("receiver_chains_storage");

            chainsStorageItem
                    .after("receiver_chains_storage_chain_state")
                    .after("receiver_chain")
                    .after("router")
                    .process(() -> {
                        try {
                            IRouter router = IOC.resolve(Keys.resolveByName(IRouter.class.getCanonicalName()));

                            IOC.register(
                                    Keys.resolveByName(IChainStorage.class.getCanonicalName()),
                                    new SingletonStrategy(new ChainStorage(new ConcurrentHashMap<>(), router)));
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException("ReceiverChainsStorage plugin can't load: can't get ReceiverChainsStorage key", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecuteException("ReceiverChainsStorage plugin can't load: can't create strategy", e);
                        } catch (RegistrationException e) {
                            throw new ActionExecuteException("ReceiverChainsStorage plugin can't load: can't register new strategy", e);
                        }
                    })
                    .revertProcess(() -> {
                        String itemName = "receiver_chains_storage";
                        String keyName = "";

                        try {
                            keyName = IChainStorage.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Unregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }
                    });

            bootstrap.add(chainsStorageItem);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
