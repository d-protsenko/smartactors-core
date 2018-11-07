package info.smart_tools.smartactors.endpoint_plugins.endpoint_response_strategy_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.endpoint.response_strategy.EndpointResponseStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;

public class EndpointResponseStrategyPlugin extends BootstrapPlugin {

    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public EndpointResponseStrategyPlugin(final IBootstrap bootstrap) {
            super(bootstrap);
    }

    @Item("http_response_strategy")
    public void registerHttpResponseStrategy()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(
                Keys.resolveByName("endpoint response strategy"),
                new SingletonStrategy(
                        new EndpointResponseStrategy()
                )
        );
    }

    @ItemRevert("http_response_strategy")
    public void unregisterHttpResponseStrategy() {
        String itemName = "http_response_strategy";
        String keyName = "endpoint response strategy";

        try {
            IOC.remove(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }
    }
}
