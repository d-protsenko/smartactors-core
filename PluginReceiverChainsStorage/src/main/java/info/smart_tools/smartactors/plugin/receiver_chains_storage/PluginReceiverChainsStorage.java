package info.smart_tools.smartactors.plugin.receiver_chains_storage;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.chain_storage.ChainStorage;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.irouter.IRouter;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;

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
            BootstrapItem chainsStorageItem = new BootstrapItem("receiver_chains_storage");

            chainsStorageItem
                    .after("receiver_chain")
                    .after("router")
                    .process(() -> {
                        try {
                            IRouter router = IOC.resolve(Keys.getOrAdd(IRouter.class.getCanonicalName()));

                            IOC.register(
                                    Keys.getOrAdd(IChainStorage.class.getCanonicalName()),
                                    new SingletonStrategy(new ChainStorage(new ConcurrentHashMap<>(), router)));
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException("ReceiverChainsStorage plugin can't load: can't get ReceiverChainsStorage key", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecuteException("ReceiverChainsStorage plugin can't load: can't create strategy", e);
                        } catch (RegistrationException e) {
                            throw new ActionExecuteException("ReceiverChainsStorage plugin can't load: can't register new strategy", e);
                        }
                    });

            bootstrap.add(chainsStorageItem);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
