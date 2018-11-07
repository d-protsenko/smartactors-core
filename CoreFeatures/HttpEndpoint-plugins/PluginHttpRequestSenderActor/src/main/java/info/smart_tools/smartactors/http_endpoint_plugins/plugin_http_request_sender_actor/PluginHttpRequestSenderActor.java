package info.smart_tools.smartactors.http_endpoint_plugins.plugin_http_request_sender_actor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.http_endpoint.http_request_sender_actor.HttpRequestSenderActor;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;

/**
 * Plugin for {@link HttpRequestSenderActor}
 */
public class PluginHttpRequestSenderActor implements IPlugin {

    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * The constructor
     *
     * @param bootstrap    the bootstrap
     */
    public PluginHttpRequestSenderActor(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> requestSenderItem = new BootstrapItem("actor:http_request_sender");

            requestSenderItem
//                    .after("IOC")
//                    .before("starter")
                    .process(() -> {
                        try {
                            IOC.register(
                                    Keys.resolveByName("HttpRequestSenderActor"),
                                    // Response sender is stateless so it's safe to use singleton strategy.
                                    new SingletonStrategy(new HttpRequestSenderActor()));
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException("RequestSenderActor plugin can't load: can't get RequestSenderActor key", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecuteException("RequestSenderActor plugin can't load: can't create strategy", e);
                        } catch (RegistrationException e) {
                            throw new ActionExecuteException("RequestSenderActor plugin can't load: can't register new strategy", e);
                        }
                    })
                    .revertProcess(() -> {
                        String itemName = "actor:http_request_sender";
                        String keyName = "HttpRequestSenderActor";

                        try {
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }
                    });

            bootstrap.add(requestSenderItem);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
