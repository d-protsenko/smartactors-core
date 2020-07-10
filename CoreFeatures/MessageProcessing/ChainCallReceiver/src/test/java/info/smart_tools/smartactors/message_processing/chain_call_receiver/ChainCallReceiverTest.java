package info.smart_tools.smartactors.message_processing.chain_call_receiver;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.ChainChoiceException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.MessageReceiveException;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Test for {@link ChainCallReceiver}.
 */
public class ChainCallReceiverTest extends PluginsLoadingTestBase {

    @Override
    protected void loadPlugins()
            throws Exception {
        load(ScopedIOCPlugin.class);
        load(PluginScopeProvider.class);
        load(PluginIOCKeys.class);
        load(IFieldNamePlugin.class);
    }


    @Override
    protected void registerMocks()
            throws Exception {
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrowWhenStrategyIsNull()
            throws Exception {
        new ChainCallReceiver(null);
    }

    @Test
    public void Should_applyStrategy()
            throws Exception {
        IChainStorage chainStorageMock = mock(IChainStorage.class);
        IChainChoiceStrategy chainChoiceStrategyMock = mock(IChainChoiceStrategy.class);

        Object chainName = "test_chain";
        IReceiverChain chainMock = mock(IReceiverChain.class);
        IMessageProcessor messageProcessorMock = mock(IMessageProcessor.class);
        IMessageProcessingSequence sequenceMock = mock(IMessageProcessingSequence.class);
        Exception exception = new InvalidArgumentException("Chain Choice Exception");
        IObject chainDescriptionMock = mock(IObject.class);
        IObject contextMock = mock(IObject.class);

        IMessageReceiver receiver = new ChainCallReceiver(chainChoiceStrategyMock);

        when(chainChoiceStrategyMock.chooseChain(same(messageProcessorMock))).thenReturn(chainName);
        when(chainStorageMock.resolve(same(chainName))).thenReturn(chainMock);
        when(messageProcessorMock.getSequence()).thenReturn(sequenceMock);
        when(messageProcessorMock.getContext()).thenReturn(contextMock);
        when(sequenceMock.getCurrentReceiverArguments()).thenReturn(chainDescriptionMock);
        when(chainMock.getChainDescription()).thenReturn(chainDescriptionMock);
        when(chainDescriptionMock.getValue(new FieldName("externalAccess"))).thenReturn(true);
        when(chainDescriptionMock.getValue(new FieldName("scopeSwitching"))).thenReturn(true);

        receiver.receive(messageProcessorMock);

        verify(sequenceMock).callChainSecurely(same(chainName),same(messageProcessorMock));

        when(chainChoiceStrategyMock.chooseChain(same(messageProcessorMock))).thenThrow(exception);

        try {
            receiver.receive(messageProcessorMock);
            fail();
        } catch (MessageReceiveException e) {
            assertSame(exception, e.getCause());
        }

        receiver.dispose();
    }
}
