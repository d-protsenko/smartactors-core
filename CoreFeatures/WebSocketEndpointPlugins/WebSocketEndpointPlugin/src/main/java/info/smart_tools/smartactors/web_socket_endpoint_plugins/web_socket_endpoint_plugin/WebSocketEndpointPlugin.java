package info.smart_tools.smartactors.web_socket_endpoint_plugins.web_socket_endpoint_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.FunctionExecutionException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.endpoint.interfaces.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.web_socket_endpoint.web_socket_endpoint.WebSocketConnectionLifecycleListener;
import info.smart_tools.smartactors.web_socket_endpoint.web_socket_endpoint.WebSocketEndpoint;
import info.smart_tools.smartactors.web_socket_endpoint.web_socket_endpoint.WebSocketSender;
import info.smart_tools.smartactors.web_socket_endpoint.web_socket_endpoint.exceptions.ConnectionListenerException;
import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WebSocketEndpointPlugin extends BootstrapPlugin {

    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public WebSocketEndpointPlugin(final IBootstrap bootstrap) {
            super(bootstrap);
    }

    @Item("register_websocket_connection_map")
    public void registerConnectionMap()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(Keys.getOrAdd("web-socket connection map"), new SingletonStrategy(new ConcurrentHashMap<Object, Channel>()));
    }

    @Item("register_websocket_message_sender")
    @After({
            "register_websocket_connection_map",
    })
    public void registerMessageSender()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(Keys.getOrAdd("websocket message sender"), new ApplyFunctionToArgumentsStrategy(args -> {
            try {
                return new WebSocketSender(IOC.resolve(Keys.getOrAdd("web-socket connection map")));
            } catch (ResolutionException e) {
                throw new FunctionExecutionException(e);
            }
        }));
    }

    @Item("register_websocket_endpoint")
    @After({
        "register_websocket_connection_map",
    })
    public void registerWSEndpoint()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IFieldName portFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "port");
        IFieldName pathFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "path");
        IFieldName maxContentLengthFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "maxContentLength");
        IFieldName startChainFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "startChain");
        IFieldName nameFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "name");

        IOC.register(Keys.getOrAdd("websocket_endpoint"), new ApplyFunctionToArgumentsStrategy(args -> {
            IObject conf = (IObject) args[0];

            try {
                ConcurrentMap<Object, Channel> connectionMap = IOC.resolve(Keys.getOrAdd("web-socket connection map"));

                WebSocketConnectionLifecycleListener connectionLifecycleListener = new WebSocketConnectionLifecycleListener() {
                    @Override
                    public void onNewConnection(final Object id, final Channel channel) throws ConnectionListenerException {
                        connectionMap.put(id, channel);
                    }

                    @Override
                    public void onClosedConnection(final Object id, final Channel channel) throws ConnectionListenerException {
                        connectionMap.remove(id, channel);
                    }
                };

                WebSocketEndpoint endpoint = new WebSocketEndpoint(
                        ((Number) conf.getValue(portFieldName)).intValue(),
                        (String) conf.getValue(pathFieldName),
                        ((Number) conf.getValue(maxContentLengthFieldName)).intValue(),
                        connectionLifecycleListener,
                        IOC.resolve(Keys.getOrAdd(IEnvironmentHandler.class.getCanonicalName()), conf),
                        ScopeProvider.getCurrentScope(),
                        (String) conf.getValue(nameFieldName),
                        (IReceiverChain) conf.getValue(startChainFieldName)
                );

                return endpoint;
            } catch (Exception e) {
                throw new FunctionExecutionException(e);
            }
        }));
    }
}
