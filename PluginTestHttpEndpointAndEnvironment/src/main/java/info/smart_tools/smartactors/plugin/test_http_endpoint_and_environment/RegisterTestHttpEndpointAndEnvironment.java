package info.smart_tools.smartactors.plugin.test_http_endpoint_and_environment;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.iasync_service.IAsyncService;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.ichannel_handler.IChannelHandler;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.initialization_exception.InitializationException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.scope.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.test.isource.ISource;
import info.smart_tools.smartactors.test.test_data_source_iobject.IObjectDataSource;
import info.smart_tools.smartactors.test.test_http_endpoint.TestChannelHandler;
import info.smart_tools.smartactors.test.test_http_endpoint.TestHttpEndpoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Plugin register creation strategies for {@link TestHttpEndpoint}, {@link TestChannelHandler} into IOC.
 */
public class RegisterTestHttpEndpointAndEnvironment implements IPlugin {

    /** Local storage for instance of {@link IBootstrap}*/
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor with single argument
     * @param bootstrap instance of {@link IBootstrap}
     * @throws InvalidArgumentException if any errors occurred
     */
    public RegisterTestHttpEndpointAndEnvironment(final IBootstrap<IBootstrapItem<String>> bootstrap)
            throws InvalidArgumentException {
        if (null == bootstrap) {
            throw new InvalidArgumentException("Incoming argument should not be null.");
        }
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("RegisterTestHttpEndpoint");
            item
                    .after("IOC")
                    .after("IFieldNamePlugin")
                    .after("iobject")
                    .after("test environment handler")
                    .before("chain tests runner")
                    .process(
                            () -> {
                                try {
                                    // Creates and registers test data source
                                    ISource<IObject, IObject> source = new IObjectDataSource();
                                    IOC.register(
                                            IOC.resolve(IOC.getKeyForKeyStorage(), "test_data_source"),
                                            new SingletonStrategy(source)
                                    );

                                    // Creates and register object with test responses
                                    List<Object> responses = new ArrayList<Object>();
                                    IOC.register(
                                            IOC.resolve(IOC.getKeyForKeyStorage(), "test_responses"),
                                            new SingletonStrategy(responses)
                                    );

                                    // Creates and registers test channel handler
                                    IChannelHandler channelHandler = new TestChannelHandler(responses);
                                    IOC.register(
                                            IOC.resolve(IOC.getKeyForKeyStorage(), TestChannelHandler.class.getCanonicalName()),
                                            new SingletonStrategy(channelHandler)
                                    );

                                    // Creates and registers test http endpoint
                                    IEnvironmentHandler handler = IOC.resolve(
                                            IOC.resolve(IOC.getKeyForKeyStorage(), "test environment handler")
                                    );
                                    IAsyncService endpoint = new TestHttpEndpoint(
                                            source,
                                            ScopeProvider.getCurrentScope(),
                                            handler,
                                            1000L,
                                            null
                                    );
                                    IOC.register(
                                            IOC.resolve(IOC.getKeyForKeyStorage(), "test_http_endpoint"),
                                            new SingletonStrategy(endpoint)
                                    );
                                    endpoint.start();
                                } catch (ScopeProviderException e) {
                                    throw new ActionExecuteException("RegisterTestHttpEndpoint plugin can't load: current scope is undefined.", e);
                                } catch (ResolutionException e) {
                                    throw new ActionExecuteException("RegisterTestHttpEndpoint plugin can't load: can't get keys for testing objects", e);
                                } catch (InvalidArgumentException e) {
                                    throw new ActionExecuteException("RegisterTestHttpEndpoint plugin can't load: can't create strategy", e);
                                } catch (RegistrationException e) {
                                    throw new ActionExecuteException("RegisterTestHttpEndpoint plugin can't load: can't register new strategy", e);
                                } catch (InitializationException e) {
                                    throw new ActionExecuteException("RegisterTestHttpEndpoint plugin can't load: can't create instance of TestHttpEndpoint", e);
                                }
                            }
                        );
            this.bootstrap.add(item);
        } catch (Throwable e) {
            throw new PluginException("Could not load 'RegisterTestHttpEndpoint plugin'", e);
        }
    }
}
