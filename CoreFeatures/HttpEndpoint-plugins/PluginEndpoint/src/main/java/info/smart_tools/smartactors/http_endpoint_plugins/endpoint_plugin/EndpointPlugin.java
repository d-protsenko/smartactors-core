package info.smart_tools.smartactors.http_endpoint_plugins.endpoint_plugin;


import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecutionException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.endpoint.interfaces.iresponse_sender.IResponseSender;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.http_endpoint.interfaces.icookies_extractor.ICookiesSetter;
import info.smart_tools.smartactors.http_endpoint.interfaces.iheaders_extractor.IHeadersExtractor;
import info.smart_tools.smartactors.http_endpoint.interfaces.iresponse_status_extractor.IResponseStatusExtractor;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_by_type_and_name_strategy.ResolveByTypeAndNameStrategy;

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
//                    .after("IOC")
//                    .after("message_processor")
//                    .after("message_processing_sequence")
                    .after("response")
                    .after("response_content_strategy")
//                    .after("FieldNamePlugin")
//                    .before("starter")
                    .process(() -> {
                        ResolveByTypeAndNameStrategy deserializationStrategyChooser = new ResolveByTypeAndNameStrategy();
                        ResolveByTypeAndNameStrategy responseSenderChooser = new ResolveByTypeAndNameStrategy();
                        ResolveByTypeAndNameStrategy cookiesSetterChooser = new ResolveByTypeAndNameStrategy();
                        ResolveByTypeAndNameStrategy headersExtractorChooser = new ResolveByTypeAndNameStrategy();
                        ResolveByTypeAndNameStrategy responseStatusExtractorChooser = new ResolveByTypeAndNameStrategy();
                        try {
                            try {
                                IOC.register(Keys.resolveByName("DeserializationStrategyChooser"),
                                        new SingletonStrategy(
                                                deserializationStrategyChooser
                                        )
                                );
                            } catch (InvalidArgumentException e) {
                                throw new RuntimeException(e);
                            }
                            IOC.register(Keys.resolveByName("info.smart_tools.smartactors.endpoint.interfaces.ideserialize_strategy.IDeserializeStrategy"),
                                    deserializationStrategyChooser
                            );

                            IOC.register(Keys.resolveByName("ResponseSenderChooser"),
                                    new SingletonStrategy(
                                            responseSenderChooser
                                    )
                            );
                            IOC.register(Keys.resolveByName(IResponseSender.class.getCanonicalName()),
                                    responseSenderChooser
                            );


                            IOC.register(Keys.resolveByName("CookiesSetterChooser"),
                                    new SingletonStrategy(
                                            cookiesSetterChooser
                                    )
                            );
                            IOC.register(Keys.resolveByName(ICookiesSetter.class.getCanonicalName()),
                                    cookiesSetterChooser
                            );

                            IOC.register(Keys.resolveByName("HeadersExtractorChooser"),
                                    new SingletonStrategy(
                                            headersExtractorChooser
                                    )
                            );
                            IOC.register(Keys.resolveByName(IHeadersExtractor.class.getCanonicalName()),
                                    headersExtractorChooser
                            );

                            IOC.register(Keys.resolveByName("ResponseStatusSetter"),
                                    new SingletonStrategy(
                                            responseStatusExtractorChooser
                                    )
                            );
                            IOC.register(Keys.resolveByName(IResponseStatusExtractor.class.getCanonicalName()),
                                    responseStatusExtractorChooser
                            );


                        } catch (RegistrationException e) {
                            throw new ActionExecutionException("\"EndpointPlugin\" plugin can't load: can't register new strategy", e);
                        } catch (ResolutionException e) {
                            throw new ActionExecutionException("\"EndpointPlugin\" plugin can't load: can't get key", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecutionException("\"EndpointPlugin\" plugin can't load: can't create strategy", e);
                        }
                    })
                    .revertProcess(() -> {
                        String itemName = "EndpointPlugin";
                        String keyName = "";

                        try {
                            keyName = IResponseStatusExtractor.class.getCanonicalName();
                            IOC.unregister(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "ResponseStatusSetter";
                            IOC.unregister(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = IHeadersExtractor.class.getCanonicalName();
                            IOC.unregister(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "HeadersExtractorChooser";
                            IOC.unregister(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = ICookiesSetter.class.getCanonicalName();
                            IOC.unregister(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "CookiesSetterChooser";
                            IOC.unregister(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = IResponseSender.class.getCanonicalName();
                            IOC.unregister(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "ResponseSenderChooser";
                            IOC.unregister(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "info.smart_tools.smartactors.endpoint.interfaces.ideserialize_strategy.IDeserializeStrategy";
                            IOC.unregister(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "DeserializationStrategyChooser";
                            IOC.unregister(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }
                    });

            bootstrap.add(item);
        } catch (Exception e) {
            throw new PluginException("Can't load \"EndpointPlugin\" plugin", e);
        }
    }
}
