package info.smart_tools.smartactors.http_endpoint_plugins.plugins_deferred_response_actor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.http_endpoint.deferred_response_actor.DeferredResponseActor;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

/**
 * Plugin for register {@link DeferredResponseActor} in IOC.
 */
public class PluginDeferredResponseActor extends BootstrapPlugin {

    public PluginDeferredResponseActor(IBootstrap bootstrap) {
        super(bootstrap);
    }

    @Item("plugin-deferred-response-actor")
    public void registerDeferredResponseActor() throws ResolutionException, InvalidArgumentException, RegistrationException {
        IOC.register(
                Keys.getOrAdd(DeferredResponseActor.class.getCanonicalName()),
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> new DeferredResponseActor()
                ));
    }

}
