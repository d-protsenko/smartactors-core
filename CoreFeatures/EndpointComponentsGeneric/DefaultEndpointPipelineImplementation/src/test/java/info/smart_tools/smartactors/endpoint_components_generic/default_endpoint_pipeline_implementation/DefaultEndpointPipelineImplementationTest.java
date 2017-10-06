package info.smart_tools.smartactors.endpoint_components_generic.default_endpoint_pipeline_implementation;

import info.smart_tools.smartactors.base.interfaces.iaction.IFunction0;
import info.smart_tools.smartactors.endpoint_components_generic.asynchronous_unordered_message_handler.AsynchronousUnorderedMessageHandler;
import info.smart_tools.smartactors.endpoint_components_generic.create_environment_message_handler.CreateEnvironmentMessageHandler;
import info.smart_tools.smartactors.endpoint_components_generic.scope_setter_message_handler.ScopeSetterMessageHandler;
import info.smart_tools.smartactors.endpoint_components_generic.send_internal_message_handler.SendInternalMessageHandler;
import info.smart_tools.smartactors.endpoint_interfaces.iendpoint_pipeline.IEndpointPipeline;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandler;
import info.smart_tools.smartactors.field_plugins.ifield_plugin.IFieldPlugin;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

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
        int stackDepth = 5;
        IQueue<ITask> taskQueueMock = mock(IQueue.class);
        IFunction0 ctxFactory = mock(IFunction0.class);
        IReceiverChain receiverChainMock = mock(IReceiverChain.class);
        IScope scopeMock = mock(IScope.class);
        List<IMessageHandler> handlers = new ArrayList<IMessageHandler>() {{
            add(new SendInternalMessageHandler<>(stackDepth, taskQueueMock, receiverChainMock));
            add(new CreateEnvironmentMessageHandler<>());
            add(new ScopeSetterMessageHandler<>(scopeMock));
            add(new AsynchronousUnorderedMessageHandler<>(taskQueueMock));
        }};

        IEndpointPipeline pipeline = new DefaultEndpointPipelineImplementation(handlers, ctxFactory);
    }
}
