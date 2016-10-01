package info.smart_tools.smartactors.plugin.endpoint_plugin;


import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.IDeserializeStrategy;
import info.smart_tools.smartactors.core.deserialization_strategy_chooser.ResolveByTypeAndNameStrategy;
import info.smart_tools.smartactors.core.icookies_extractor.ICookiesSetter;
import info.smart_tools.smartactors.core.iheaders_extractor.IHeadersExtractor;
import info.smart_tools.smartactors.core.iresponse_sender.IResponseSender;
import info.smart_tools.smartactors.core.iresponse_status_extractor.IResponseStatusExtractor;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

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
                                ResolveByTypeAndNameStrategy responseSenderChooser = new ResolveByTypeAndNameStrategy();
                                ResolveByTypeAndNameStrategy cookiesSetterChooser = new ResolveByTypeAndNameStrategy();
                                ResolveByTypeAndNameStrategy headersExtractorChooser = new ResolveByTypeAndNameStrategy();
                                ResolveByTypeAndNameStrategy responseStatusExtractorChooser = new ResolveByTypeAndNameStrategy();
                                try {
                                    try {
                                        IOC.register(Keys.getOrAdd("DeserializationStrategyChooser"),
                                                new SingletonStrategy(
                                                        deserializationStrategyChooser
                                                )
                                        );
                                    } catch (InvalidArgumentException e) {
                                        throw new RuntimeException(e);
                                    }
                                    IOC.register(Keys.getOrAdd(IDeserializeStrategy.class.getCanonicalName()),
                                            deserializationStrategyChooser
                                    );

                                    IOC.register(Keys.getOrAdd("ResponseSenderChooser"),
                                            new SingletonStrategy(
                                                    responseSenderChooser
                                            )
                                    );
                                    IOC.register(Keys.getOrAdd(IResponseSender.class.getCanonicalName()),
                                            responseSenderChooser
                                    );


                                    IOC.register(Keys.getOrAdd("CookiesSetterChooser"),
                                            new SingletonStrategy(
                                                    cookiesSetterChooser
                                            )
                                    );
                                    IOC.register(Keys.getOrAdd(ICookiesSetter.class.getCanonicalName()),
                                            cookiesSetterChooser
                                    );

                                    IOC.register(Keys.getOrAdd("HeadersExtractorChooser"),
                                            new SingletonStrategy(
                                                    headersExtractorChooser
                                            )
                                    );
                                    IOC.register(Keys.getOrAdd(IHeadersExtractor.class.getCanonicalName()),
                                            headersExtractorChooser
                                    );

                                    IOC.register(Keys.getOrAdd("ResponseStatusSetter"),
                                            new SingletonStrategy(
                                                    responseStatusExtractorChooser
                                            )
                                    );
                                    IOC.register(Keys.getOrAdd(IResponseStatusExtractor.class.getCanonicalName()),
                                            responseStatusExtractorChooser
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
            bootstrap.add(item);
        } catch (Exception e) {
            throw new PluginException("Can't load \"EndpointPlugin\" plugin", e);
        }
    }
}
