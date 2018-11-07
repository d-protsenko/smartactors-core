package info.smart_tools.smartactors.message_processing.response_sender_receiver;

import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_processing_interfaces.iresponse_strategy.IResponseStrategy;
import info.smart_tools.smartactors.message_processing_interfaces.iresponse_strategy.exceptions.ResponseException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.MessageReceiveException;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Test for {@link ResponseSenderReceiver}.
 */
public class ResponseSenderReceiverTest extends PluginsLoadingTestBase {
    private IMessageProcessor messageProcessorMock;
    private IObject envMock, ctxMock;
    private IResponseStrategy responseStrategyMock;

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
        messageProcessorMock = mock(IMessageProcessor.class);
        envMock = mock(IObject.class);
        ctxMock = mock(IObject.class);
        responseStrategyMock = mock(IResponseStrategy.class);

        IOC.register(Keys.resolveByName("send response action"), new SingletonStrategy(new ResponseSenderAction()));

        when(envMock.getValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "context")))
                .thenReturn(ctxMock);
        when(messageProcessorMock.getContext()).thenReturn(ctxMock);
        when(messageProcessorMock.getEnvironment()).thenReturn(envMock);
        when(ctxMock.getValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "responseStrategy")))
                .thenReturn(responseStrategyMock);
    }

    @Test
    public void Should_sendResponseUsingResponseStrategyFromContext()
            throws Exception {
        IMessageReceiver receiver = new ResponseSenderReceiver();

        receiver.receive(messageProcessorMock);

        verify(responseStrategyMock).sendResponse(same(envMock));

        receiver.dispose();
    }

    @Test(expected = MessageReceiveException.class)
    public void Should_wrapExceptionOccurredSendingResponse()
            throws Exception {
        doThrow(ResponseException.class).when(responseStrategyMock).sendResponse(same(envMock));

        IMessageReceiver receiver = new ResponseSenderReceiver();

        receiver.receive(messageProcessorMock);
    }
}
