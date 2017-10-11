package info.smart_tools.smartactors.endpoint_components_generic.default_endpoint_pipeline_implementation;

import info.smart_tools.smartactors.base.interfaces.iaction.IFunction0;
import info.smart_tools.smartactors.endpoint_interfaces.iendpoint_pipeline.IEndpointPipeline;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageContext;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandler;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandlerCallback;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.exception.MessageHandlerException;
import info.smart_tools.smartactors.field_plugins.ifield_plugin.IFieldPlugin;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class DefaultEndpointPipelineImplementationTest extends PluginsLoadingTestBase {
    @Override
    protected void loadPlugins() throws Exception {
        load(ScopedIOCPlugin.class);
        load(PluginScopeProvider.class);
        load(PluginIOCKeys.class);
        load(PluginDSObject.class);
        load(IFieldNamePlugin.class);
        load(IFieldPlugin.class);
    }

    @Override
    protected void registerMocks() throws Exception {}

    @Test
    public void Should_buildPipeline()
            throws Exception {
        IMessageContext ctx = mock(IMessageContext.class);
        IFunction0 ctxFactory = mock(IFunction0.class);
        when(ctxFactory.execute()).thenReturn(ctx);

        IMessageHandler handler1 = mock(IMessageHandler.class);
        IMessageHandler handler2 = mock(IMessageHandler.class);

        List<IMessageHandler> handlers = new ArrayList<IMessageHandler>() {{
            add(handler1);
            add(handler2);
        }};

        IEndpointPipeline pipeline = new DefaultEndpointPipelineImplementation(handlers, ctxFactory);
        pipeline.getInputCallback().handle(ctx);
        verify(handler1).handle(anyObject(), eq(ctx));

        assertSame(pipeline.getHandlers(), handlers);
        assertSame(pipeline.getContextFactory(), ctxFactory);
    }

    @Test(expected = MessageHandlerException.class)
    public void Should_thowErrorWhenNoHandlers() throws Exception {
        IMessageContext ctx = mock(IMessageContext.class);
        IFunction0 ctxFactory = mock(IFunction0.class);
        when(ctxFactory.execute()).thenReturn(ctx);
        IEndpointPipeline pipeline = new DefaultEndpointPipelineImplementation(new ArrayList<>(), ctxFactory);
        pipeline.getInputCallback().handle(ctx);
    }

    @Test
    public void Should_notBeStacjOverfow() throws Exception {
        IMessageContext ctx = mock(IMessageContext.class);
        IFunction0 ctxFactory = mock(IFunction0.class);
        when(ctxFactory.execute()).thenReturn(ctx);

        IMessageHandler handler1 = new IMessageHandler() {
            @Override
            public void handle(IMessageHandlerCallback next, IMessageContext context) throws MessageHandlerException {
                next.handle(ctx);
            }
        };


        List<IMessageHandler> handlers = new ArrayList<IMessageHandler>() {{
            add(handler1);
        }};

        IEndpointPipeline pipeline = new DefaultEndpointPipelineImplementation(handlers, ctxFactory);
        pipeline.getInputCallback().handle(ctx);
    }
}
