package info.smart_tools.smartactors.message_processing_plugins.immutable_receiver_chain_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;
import info.smart_tools.smartactors.message_processing.receiver_chain.ImmutableReceiverChainResolutionStrategy;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;

/**
 *
 */
public class PluginReceiverChain implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public PluginReceiverChain(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            BootstrapItem receiverChainItem = new BootstrapItem("receiver_chain");

            receiverChainItem
                    .after("IOC")
                    .process(() -> {
                        try {
                            IOC.register(
                                    Keys.getOrAdd(IReceiverChain.class.getCanonicalName()),
                                    new ImmutableReceiverChainResolutionStrategy());
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException("ReceiverChain plugin can't load: can't get ReceiverChain key", e);
                        } catch (RegistrationException e) {
                            throw new ActionExecuteException("ReceiverChain plugin can't load: can't register new strategy", e);
                        }
                    })
                    .revertProcess(() -> {
                        String itemName = "receiver_chain";
                        String keyName = "";

                        try {
                            keyName = IReceiverChain.class.getCanonicalName();
                            IOC.remove(Keys.getOrAdd(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                    });

            bootstrap.add(receiverChainItem);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
