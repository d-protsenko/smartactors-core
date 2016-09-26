package info.smart_tools.smartactors.plugin.response_content_json_strategy;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.iresponse_content_strategy.IResponseContentStrategy;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.plugin.response_environment_http_strategy.response_content_json_strategy.ResponseContentJsonStrategy;

/**
 *
 */
public class PluginResponseJsonContentStrategy implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap the bootstrap
     */
    public PluginResponseJsonContentStrategy(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {

            IBootstrapItem<String> item = new BootstrapItem("response_content_strategy");

            item
                    .after("IOC")
                    .process(() -> {
                        try {
                            IOC.register(
                                    Keys.getOrAdd(IResponseContentStrategy.class.getCanonicalName()),
                                    new SingletonStrategy(new ResponseContentJsonStrategy()));
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException("ResponseJsonContentStrategy plugin can't load: can't get ResponseJsonContentStrategy key", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecuteException("ResponseJsonContentStrategy plugin can't load: can't create strategy", e);
                        } catch (RegistrationException e) {
                            throw new ActionExecuteException("ResponseJsonContentStrategy plugin can't load: can't register new strategy", e);
                        }
                    });

            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
