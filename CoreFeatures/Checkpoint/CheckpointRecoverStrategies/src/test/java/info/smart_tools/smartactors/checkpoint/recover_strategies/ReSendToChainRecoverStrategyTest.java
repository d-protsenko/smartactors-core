package info.smart_tools.smartactors.checkpoint.recover_strategies;

import info.smart_tools.smartactors.checkpoint.recover_strategies.chain_choice.IRecoveryChainChoiceStrategy;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_bus.interfaces.imessage_bus_handler.IMessageBusHandler;
import info.smart_tools.smartactors.message_bus.message_bus.MessageBus;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Test for {@link ReSendToChainRecoverStrategy}.
 */
public class ReSendToChainRecoverStrategyTest extends PluginsLoadingTestBase {
    private IRecoveryChainChoiceStrategy recoveryChainChoiceStrategy;

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
        recoveryChainChoiceStrategy = mock(IRecoveryChainChoiceStrategy.class);
    }

    @Test
    public void Should_initializeChainChoiceStrategy()
            throws Exception {
        IObject stateMock = mock(IObject.class);
        IObject argsMock = mock(IObject.class);

        new ReSendToChainRecoverStrategy(recoveryChainChoiceStrategy).init(stateMock, argsMock, mock(IMessageProcessor.class));

        verify(recoveryChainChoiceStrategy).init(same(stateMock), same(argsMock));
    }

    @Test
    public void Should_reSendMessageToChainChosenByIRCCS()
            throws Exception {
        IObject state = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"),
                ("{" +
                        "'responsibleCheckpointId':'rcid'," +
                        "'entryId':'eid'," +
                        "'prevCheckpointEntryId':'pceid'," +
                        "'prevCheckpointId':'pcid'," +
                        "'message':{'is-a-message':true}" +
                        "}").replace('\'','"'));

        IMessageBusHandler messageBusHandler = mock(IMessageBusHandler.class);

        ScopeProvider.getCurrentScope().setValue(MessageBus.getMessageBusKey(), messageBusHandler);

        Object chainId = new Object();

        when(recoveryChainChoiceStrategy.chooseRecoveryChain(same(state))).thenReturn(chainId);

        new ReSendToChainRecoverStrategy(recoveryChainChoiceStrategy).reSend(state);

        ArgumentCaptor<IObject> mc = ArgumentCaptor.forClass(IObject.class);

        verify(messageBusHandler).handle(mc.capture(), same(chainId));


        assertNotNull(mc.getValue());
        assertEquals(true, mc.getValue().getValue(IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "is-a-message")));

        IObject checkpointStatus =
                (IObject) mc.getValue().getValue(IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "checkpointStatus"));

        assertNotNull(checkpointStatus);
        assertEquals("rcid",
                checkpointStatus.getValue(IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "responsibleCheckpointId")));
        assertEquals("eid",
                checkpointStatus.getValue(IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "checkpointEntryId")));
        assertEquals("pceid",
                checkpointStatus.getValue(IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "prevCheckpointEntryId")));
        assertEquals("pcid",
                checkpointStatus.getValue(IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "prevCheckpointId")));
    }
}
