package info.smart_tools.smartactors.endpoint_components_generic.default_message_handler_pipeline;

import info.smart_tools.smartactors.endpoint_components_generic.asynchronous_unordered_message_handler.AsynchronousUnorderedMessageHandler;
import info.smart_tools.smartactors.endpoint_components_generic.create_environment_message_handler.CreateEnvironmentMessageHandler;
import info.smart_tools.smartactors.endpoint_components_generic.scope_setter_message_handler.ScopeSetterMessageHandler;
import info.smart_tools.smartactors.endpoint_components_generic.send_internal_message_handler.SendInternalMessageHandler;
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
import org.junit.Ignore;
import org.junit.Test;

import static jdk.nashorn.internal.codegen.OptimisticTypesPersistence.load;
import static org.mockito.Mockito.mock;

@Ignore
public class DefaultStaticMessageHandlerPipelineBuilderTest extends PluginsLoadingTestBase {
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
        IReceiverChain receiverChainMock = mock(IReceiverChain.class);
        IScope scopeMock = mock(IScope.class);

        DefaultStaticMessageHandlerPipelineBuilder.create()
                .add(new SendInternalMessageHandler<>(stackDepth, taskQueueMock, receiverChainMock))
                .add(new CreateEnvironmentMessageHandler<>())
                .add(new ScopeSetterMessageHandler<>(scopeMock))
                .add(new AsynchronousUnorderedMessageHandler<>(taskQueueMock))
                .finish()
        ;
    }
}
