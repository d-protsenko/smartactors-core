package info.smart_tools.smartactors.web_socket_endpoint.connection_lifecycle_monitor;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import info.smart_tools.smartactors.web_socket_endpoint.web_socket_endpoint_interfaces.IWebSocketConnectionLifecycleListener;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.concurrent.GenericFutureListener;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Predicate;

import static info.smart_tools.smartactors.web_socket_endpoint.connection_lifecycle_monitor.ChannelAttributes.CONNECTION_ID_ATTRIBUTE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Test for {@link WebSocketConnectionLifecycleMonitor}.
 */
public class WebSocketConnectionLifecycleMonitorTest extends PluginsLoadingTestBase {
    @Override
    protected void loadPlugins() throws Exception {
        load(ScopedIOCPlugin.class);
        load(PluginScopeProvider.class);
        load(PluginIOCKeys.class);
        load(PluginDSObject.class);
        load(IFieldNamePlugin.class);
    }

    private final Object EVENT_OBJECT = new Object();

    private IWebSocketConnectionLifecycleListener lifecycleListenerMock;
    private IResolveDependencyStrategy connectionIdResolutionStrategyMock;
    private Predicate<Object> predicateMock;
    private ChannelHandlerContext channelHandlerContextMock;
    private Channel channelMock;
    private Attribute connectionIdAttribute;
    private ChannelFuture closeFutureMock;

    @Override
    protected void registerMocks() throws Exception {
        lifecycleListenerMock = mock(IWebSocketConnectionLifecycleListener.class);
        connectionIdResolutionStrategyMock = mock(IResolveDependencyStrategy.class);
        predicateMock = mock(Predicate.class);

        when(connectionIdResolutionStrategyMock.resolve(any()))
                .thenReturn("id-1-1").thenReturn("id-1-2").thenReturn("id-2-1")
                .thenThrow(ResolveDependencyStrategyException.class);

        channelMock = mock(Channel.class);
        channelHandlerContextMock = mock(ChannelHandlerContext.class);
        connectionIdAttribute = mock(Attribute.class);
        closeFutureMock = mock(ChannelFuture.class);

        when(channelHandlerContextMock.channel()).thenReturn(channelMock);
        when(channelMock.attr(eq(CONNECTION_ID_ATTRIBUTE))).thenReturn(connectionIdAttribute);
        when(channelMock.closeFuture()).thenReturn(closeFutureMock);
        when(closeFutureMock.channel()).thenReturn(channelMock);
    }

    @Test
    public void Should_detectOpenConnection()
            throws Exception {
        WebSocketConnectionLifecycleMonitor monitor = new WebSocketConnectionLifecycleMonitor(
                predicateMock, lifecycleListenerMock, connectionIdResolutionStrategyMock);

        when(predicateMock.test(same(EVENT_OBJECT))).thenReturn(true);

        monitor.userEventTriggered(channelHandlerContextMock, EVENT_OBJECT);

        verify(lifecycleListenerMock).onNewConnection(eq("id-1-1"), same(channelMock));
        verify(connectionIdAttribute).setIfAbsent(eq("id-1-1"));
        verify(closeFutureMock).addListener(any());
        verify(channelHandlerContextMock).fireUserEventTriggered(same(EVENT_OBJECT));
    }

    @Test
    public void Should_ignoreUnknownUserEvents()
            throws Exception {
        WebSocketConnectionLifecycleMonitor monitor = new WebSocketConnectionLifecycleMonitor(
                predicateMock, lifecycleListenerMock, connectionIdResolutionStrategyMock);

        when(predicateMock.test(same(EVENT_OBJECT))).thenReturn(false);

        monitor.userEventTriggered(channelHandlerContextMock, EVENT_OBJECT);

        verify(lifecycleListenerMock, times(0)).onNewConnection(eq("id-1-1"), same(channelMock));
        verify(connectionIdAttribute, times(0)).setIfAbsent(eq("id-1-1"));
        verify(closeFutureMock, times(0)).addListener(any());
        verify(channelHandlerContextMock).fireUserEventTriggered(same(EVENT_OBJECT));
    }

    @Test
    public void Should_detectClosedConnection()
            throws Exception {
        WebSocketConnectionLifecycleMonitor monitor = new WebSocketConnectionLifecycleMonitor(
                predicateMock, lifecycleListenerMock, connectionIdResolutionStrategyMock);

        when(predicateMock.test(same(EVENT_OBJECT))).thenReturn(true);

        monitor.userEventTriggered(channelHandlerContextMock, EVENT_OBJECT);

        ArgumentCaptor<GenericFutureListener> listenerCaptor = ArgumentCaptor.forClass(GenericFutureListener.class);
        ArgumentCaptor<Object> idCaptor = ArgumentCaptor.forClass(Object.class);
        verify(closeFutureMock).addListener(listenerCaptor.capture());
        verify(connectionIdAttribute).setIfAbsent(idCaptor.capture());
        when(connectionIdAttribute.get()).thenReturn(idCaptor.getValue());

        listenerCaptor.getValue().operationComplete(closeFutureMock);

        verify(lifecycleListenerMock).onClosedConnection(eq(idCaptor.getValue()), same(channelMock));
    }
}
