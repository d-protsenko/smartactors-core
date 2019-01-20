package info.smart_tools.smartactors.endpoint.response_strategy;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;
import info.smart_tools.smartactors.endpoint.interfaces.ichannel_handler.IChannelHandler;
import info.smart_tools.smartactors.endpoint.interfaces.iresponse.IResponse;
import info.smart_tools.smartactors.endpoint.interfaces.iresponse_content_strategy.IResponseContentStrategy;
import info.smart_tools.smartactors.endpoint.interfaces.iresponse_sender.IResponseSender;
import info.smart_tools.smartactors.endpoint.interfaces.iresponse_sender.exceptions.ResponseSendingException;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_processing_interfaces.iresponse_strategy.exceptions.ResponseException;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Test for {@link EndpointResponseStrategy}.
 */
public class EndpointResponseStrategyTest extends PluginsLoadingTestBase {
    private IObject environment;
    private IObject context;
    private IObject responseObj;
    private IResponseSender responseSender;
    private IChannelHandler channelHandler;
    private IResponse response;
    private IResponseContentStrategy responseContentStrategy;
    private Object request;

    @Override
    protected void loadPlugins() throws Exception {
        load(ScopedIOCPlugin.class);
        load(PluginScopeProvider.class);
        load(PluginIOCKeys.class);
        load(PluginDSObject.class);
        load(IFieldNamePlugin.class);
    }

    @Override
    protected void registerMocks() throws Exception {
        environment = IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.iobject.IObject"));
        context = IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.iobject.IObject"));
        responseObj = IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.iobject.IObject"));

        responseSender = mock(IResponseSender.class);
        channelHandler = mock(IChannelHandler.class);
        response = mock(IResponse.class);
        responseContentStrategy = mock(IResponseContentStrategy.class);

        environment.setValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "context"), context);
        environment.setValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "response"), responseObj);
        context.setValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "endpointName"), "theEpName");
        context.setValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "channel"), channelHandler);

        request = mock(Object.class);

        IResolutionStrategy strategy = mock(IResolutionStrategy.class);
        when(strategy.resolve()).thenReturn(response).thenThrow(ResolutionStrategyException.class);
        IOC.register(Keys.resolveByName(IResponse.class.getCanonicalName()), strategy);

        strategy = mock(IResolutionStrategy.class);
        when(strategy.resolve(same(environment))).thenReturn(responseContentStrategy);
        IOC.register(Keys.resolveByName(IResponseContentStrategy.class.getCanonicalName()), strategy);

        strategy = mock(IResolutionStrategy.class);
        when(strategy.resolve(same(request), eq("theEpName"))).thenReturn(responseSender);
        IOC.register(Keys.resolveByName(IResponseSender.class.getCanonicalName()), strategy);

        strategy = mock(IResolutionStrategy.class);
        when(strategy.resolve(same(environment))).thenReturn(request);
        IOC.register(Keys.resolveByName("http_request_key_for_response_sender"), strategy);
    }

    @Test
    public void Should_sendResponse()
            throws Exception {
        new EndpointResponseStrategy().sendResponse(environment);

        verify(responseContentStrategy).setContent(responseObj, response);
        verify(responseSender).send(same(response), same(environment), same(channelHandler));
        assertEquals(Boolean.TRUE, context.getValue(
                IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "sendResponseOnChainEnd")));
    }

    @Test(expected = ResponseException.class)
    public void Should_wrapExceptions()
            throws Exception {
        doThrow(ResponseSendingException.class).when(responseSender).send(any(), any(), any());

        new EndpointResponseStrategy().sendResponse(environment);
    }
}
