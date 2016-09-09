package info.smart_tools.smartactors.plugin;

import info.smart_tools.smartactors.core.IDeserializeStrategy;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.deserialization_strategy_chooser.ResolveByTypeAndNameStrategy;
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
 * Root plugin for all endpoints
 */
public class EndpointPlugin implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     *
     * @param bootstrap bootstrap
     */
    public EndpointPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        IBootstrapItem<String> item = null;
        try {
            item = new BootstrapItem("EndpointPlugin");
            item
                    .after("IOC")
                    .after("message_processor")
                    .after("message_processing_sequence")
                    .after("response")
                    .after("response_content_strategy")
                    .after("FieldNamePlugin")
                    .before("starter")
                    .process(
                            () -> {
                                ResolveByTypeAndNameStrategy deserializationStrategyChooser = new ResolveByTypeAndNameStrategy();
                                try {
                                    IOC.register(Keys.getOrAdd("ResolveByTypeAndNameStrategy"),
                                            new SingletonStrategy(
                                                    deserializationStrategyChooser
                                            )
                                    );
                                    IOC.register(Keys.getOrAdd(IDeserializeStrategy.class.getCanonicalName()),
                                            deserializationStrategyChooser
                                    );
                                } catch (RegistrationException e) {
                                    throw new ActionExecuteException("\"EndpointPlugin\" plugin can't load: can't register new strategy", e);
                                } catch (ResolutionException e) {
                                    throw new ActionExecuteException("\"EndpointPlugin\" plugin can't load: can't get key", e);
                                } catch (InvalidArgumentException e) {
                                    throw new ActionExecuteException("\"EndpointPlugin\" plugin can't load: can't create strategy", e);
                                }

                            }
                    );
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load \"EndpointPlugin\" plugin", e);
        }
    }
}
