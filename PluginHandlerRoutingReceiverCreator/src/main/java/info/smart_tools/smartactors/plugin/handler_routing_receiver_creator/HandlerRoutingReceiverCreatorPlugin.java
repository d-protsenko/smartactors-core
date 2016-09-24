package info.smart_tools.smartactors.plugin.handler_routing_receiver_creator;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.handler_routing_receiver_creator.HandlerRoutingReceiverCreator;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.iroutable_object_creator.IRoutedObjectCreator;
import info.smart_tools.smartactors.core.iroutable_object_creator.exceptions.ObjectCreationException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;

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
                    .process(
                            () -> {
                                try {
                                    HandlerRoutingReceiverCreator objectCreator = new HandlerRoutingReceiverCreator();
                                    IOC.register(
                                            IOC.resolve(
                                                    IOC.getKeyForKeyStorage(),
                                                    IRoutedObjectCreator.class.getCanonicalName() + "#stateless_actor"
                                            ),
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
                    );
            this.bootstrap.add(item);
        } catch (Throwable e) {
            throw new PluginException("Could not load 'HandlerRoutingReceiver plugin'", e);
        }
    }
}
