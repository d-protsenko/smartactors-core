package info.smart_tools.smartactors.plugin.response_sender_actor;

import info.smart_tools.smartactors.actor.response_sender_actor.ResponseSenderActor;
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
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;

/**
 *
 */
public class PluginResponseSenderActor implements IPlugin {
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * The constructor
     *
     * @param bootstrap    the bootstrap
     */
    public PluginResponseSenderActor(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> responseSenderItem = new BootstrapItem("actor:response_sender");

            responseSenderItem
                    .after("IOC")
                    .before("configure")
                    .process(() -> {
                        try {
                            IOC.register(
                                    Keys.getOrAdd("ResponseSenderActor"),
                                    // Response sender is stateless so it's safe to use singleton strategy.
                                    new SingletonStrategy(new ResponseSenderActor()));
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException("ResponseSenderActor plugin can't load: can't get ResponseSenderActor key", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecuteException("ResponseSenderActor plugin can't load: can't get create strategy", e);
                        } catch (RegistrationException e) {
                            throw new ActionExecuteException("ResponseSenderActor plugin can't load: can't get register new strategy", e);
                        }
                    });

            bootstrap.add(responseSenderItem);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
