package info.smart_tools.smartactors.message_processing_plugins.response_sender_receiver_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing.response_sender_receiver.ResponseSenderReceiver;

public class ResponseSenderReceiverPlugin extends BootstrapPlugin {

    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public ResponseSenderReceiverPlugin(final IBootstrap bootstrap) {
            super(bootstrap);
    }

    @Item("response_sender_receiver")
    public void doSomeThing()
        throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(Keys.getOrAdd("response sender receiver"), new SingletonStrategy(new ResponseSenderReceiver()));
    }
}
