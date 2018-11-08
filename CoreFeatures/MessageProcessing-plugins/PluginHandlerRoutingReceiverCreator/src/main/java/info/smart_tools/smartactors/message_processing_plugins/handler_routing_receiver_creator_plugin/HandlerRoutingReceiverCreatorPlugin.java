package info.smart_tools.smartactors.message_processing_plugins.handler_routing_receiver_creator_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
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
import info.smart_tools.smartactors.message_processing.handler_routing_receiver_creator.HandlerRoutingReceiverCreator;
import info.smart_tools.smartactors.message_processing_interfaces.iroutable_object_creator.IRoutedObjectCreator;
import info.smart_tools.smartactors.message_processing_interfaces.iroutable_object_creator.exceptions.ObjectCreationException;

/**
 * Implementation of {@link IPlugin}.
 * Plugin creates new instance of {@link HandlerRoutingReceiverCreator} and
 * register its into IOC.
 */
public class HandlerRoutingReceiverCreatorPlugin implements IPlugin {

    /** Local storage for instance of {@link IBootstrap} */
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * @param bootstrap Target bootstrap for adding strategy
     * @throws InvalidArgumentException if any errors occurred
     */
    public HandlerRoutingReceiverCreatorPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap)
            throws InvalidArgumentException {
        if (null == bootstrap) {
            throw new InvalidArgumentException("Incoming argument should not be null.");
        }
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("HandlerRoutingReceiverCreator");
            item
                    .after("IOC")
                    .before("starter")
                    .after("InitializeReceiverGenerator")
                    .after("InitializeWrapperGenerator")
                    .after("IFieldPlugin")
                    .after("IFieldNamePlugin")
                    .process(() -> {
                                try {
                                    HandlerRoutingReceiverCreator objectCreator = new HandlerRoutingReceiverCreator();
                                    IOC.register(
                                            Keys.resolveByName(IRoutedObjectCreator.class.getCanonicalName() + "#stateless_actor"),
                                            new SingletonStrategy(objectCreator)
                                    );
                                } catch (ResolutionException e) {
                                    throw new ActionExecuteException("HandlerRoutingReceiverCreator plugin can't load: can't get HandlerRoutingReceiverCreator key", e);
                                } catch (InvalidArgumentException e) {
                                    throw new ActionExecuteException("HandlerRoutingReceiverCreator plugin can't load: can't create strategy", e);
                                } catch (RegistrationException e) {
                                    throw new ActionExecuteException("HandlerRoutingReceiverCreator plugin can't load: can't register new strategy", e);
                                } catch (ObjectCreationException e) {
                                    throw new ActionExecuteException("HandlerRoutingReceiverCreator plugin can't load: constructor error", e);
                                }
                            }
                    )
                    .revertProcess(() -> {
                        String itemName = "HandlerRoutingReceiverCreator";
                        String keyName = "";

                        try {
                            keyName = IRoutedObjectCreator.class.getCanonicalName() + "#stateless_actor";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }
                    });
            this.bootstrap.add(item);
        } catch (Throwable e) {
            throw new PluginException("Could not load 'HandlerRoutingReceiver plugin'", e);
        }
    }
}
