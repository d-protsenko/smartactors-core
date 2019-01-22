package info.smart_tools.smartactors.message_processing.wrapper_creator_receiver_decorator;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.MessageReceiveException;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertSame;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;


public class WrapperCreatorReceiverDecoratorTest extends PluginsLoadingTestBase {
    private IResolutionStrategy wrapperResolutionStrategyMock;
    private IResolutionStrategy wrapperResolutionStrategyResolutionStrategyMock;
    private IMessageProcessor messageProcessorMock;
    private IMessageProcessingSequence sequenceMock;
    private IObject envMock, wrapperMock, stepConfMock, wrapperConfMock;
    private Map<Object, IResolutionStrategy> map;
    private IMessageReceiver receiverMock;

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
        wrapperResolutionStrategyMock = mock(IResolutionStrategy.class);
        wrapperResolutionStrategyResolutionStrategyMock = mock(IResolutionStrategy.class);
        messageProcessorMock = mock(IMessageProcessor.class);
        sequenceMock = mock(IMessageProcessingSequence.class);
        envMock = mock(IObject.class);
        wrapperMock = mock(IObject.class);
        stepConfMock = mock(IObject.class);
        wrapperConfMock = mock(IObject.class);

        IOC.register(Keys.resolveByName("the wrapper resolution strategy resolution strategy"),
                wrapperResolutionStrategyResolutionStrategyMock);

        when(wrapperResolutionStrategyResolutionStrategyMock.resolve(same(wrapperConfMock)))
                .thenReturn(wrapperResolutionStrategyMock)
                .thenThrow(ResolutionStrategyException.class);

        when(messageProcessorMock.getEnvironment()).thenReturn(envMock);
        when(messageProcessorMock.getSequence()).thenReturn(sequenceMock);

        when(wrapperResolutionStrategyMock.resolve(same(envMock)))
                .thenReturn(wrapperMock);

        when(sequenceMock.getCurrentReceiverArguments()).thenReturn(stepConfMock);
        when(stepConfMock.getValue(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "wrapper"))).thenReturn(wrapperConfMock);

        receiverMock = mock(IMessageReceiver.class);

        map = new HashMap<>();
    }

    @Test
    public void Should_resolveWrapperAndPushAsEnvironment()
            throws Exception {
        IMessageReceiver decorator = new WrapperCreatorReceiverDecorator(
                receiverMock, map, "the wrapper resolution strategy resolution strategy");

        decorator.receive(messageProcessorMock);

        verify(messageProcessorMock, times(1)).pushEnvironment(same(wrapperMock));
        verify(wrapperResolutionStrategyResolutionStrategyMock, times(1)).resolve(same(wrapperConfMock));
        assertSame(wrapperResolutionStrategyMock, map.get(stepConfMock));
        decorator.receive(messageProcessorMock);

        verify(messageProcessorMock, times(2)).pushEnvironment(same(wrapperMock));
        verify(wrapperResolutionStrategyResolutionStrategyMock, times(1)).resolve(same(wrapperConfMock));
        assertSame(wrapperResolutionStrategyMock, map.get(stepConfMock));

        decorator.dispose();
        verify(receiverMock).dispose();

        doThrow(new RuntimeException("test")).when(receiverMock).dispose();
        decorator.dispose();
    }

    @Test(expected = MessageReceiveException.class)
    public void Should_wrapExceptions()
            throws Exception {
        IMessageReceiver decorator = new WrapperCreatorReceiverDecorator(
                receiverMock, map, "the wrapper resolution strategy resolution strategy");

        when(wrapperResolutionStrategyResolutionStrategyMock.resolve(any()))
                .thenThrow(ResolutionStrategyException.class);

        decorator.receive(messageProcessorMock);
    }
}
