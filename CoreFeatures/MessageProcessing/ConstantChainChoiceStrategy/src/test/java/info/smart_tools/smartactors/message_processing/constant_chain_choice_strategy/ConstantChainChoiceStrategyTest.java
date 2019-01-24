package info.smart_tools.smartactors.message_processing.constant_chain_choice_strategy;

import info.smart_tools.smartactors.base.interfaces.istrategy.IStrategy;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_processing.chain_call_receiver.IChainChoiceStrategy;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link ConstantChainChoiceStrategy}.
 */
public class ConstantChainChoiceStrategyTest extends PluginsLoadingTestBase {
    private IStrategy chainIdStrategy;
    private IMessageProcessor messageProcessorMock;
    private IMessageProcessingSequence messageProcessingSequenceMock;
    private Object id = new Object();

    @Override
    protected void loadPlugins()
            throws Exception {
        load(ScopedIOCPlugin.class);
        load(PluginScopeProvider.class);
        load(PluginIOCKeys.class);
        load(PluginDSObject.class);
        load(IFieldNamePlugin.class);
    }


    @Override
    protected void registerMocks()
            throws Exception {
        chainIdStrategy = mock(IStrategy.class);
        messageProcessorMock = mock(IMessageProcessor.class);
        messageProcessingSequenceMock = mock(IMessageProcessingSequence.class);
        IObject args = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"), "{'chain':'chain_to_call_name'}".replace('\'', '"'));

        when(messageProcessorMock.getSequence()).thenReturn(messageProcessingSequenceMock);
        when(messageProcessingSequenceMock.getCurrentReceiverArguments()).thenReturn(args);
        when(chainIdStrategy.resolve(eq("chain_to_call_name"))).thenReturn(id);

        IOC.register(Keys.getKeyByName("chain_id_from_map_name_and_message"), chainIdStrategy);
    }

    @Test
    public void Should_chooseChainUsingIdentifierInCurrentStepArguments()
            throws Exception {
        IChainChoiceStrategy strategy = new ConstantChainChoiceStrategy();

        Object chainName = strategy.chooseChain(messageProcessorMock);
        assertEquals(chainName, "chain_to_call_name");
    }

    @Test(expected = ReadValueException.class)
    public void Should_ThrowException()
            throws Exception {

        when(messageProcessingSequenceMock.getCurrentReceiverArguments()).thenThrow(ReadValueException.class);

        IChainChoiceStrategy strategy = new ConstantChainChoiceStrategy();

        strategy.chooseChain(messageProcessorMock);
    }
}