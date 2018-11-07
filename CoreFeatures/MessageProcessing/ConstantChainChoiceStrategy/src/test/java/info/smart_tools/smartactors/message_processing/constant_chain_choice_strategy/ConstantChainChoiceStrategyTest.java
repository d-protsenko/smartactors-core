package info.smart_tools.smartactors.message_processing.constant_chain_choice_strategy;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_processing.chain_call_receiver.IChainChoiceStrategy;
import info.smart_tools.smartactors.message_processing.message_processing_sequence.exceptions.ChainChoiceException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link ConstantChainChoiceStrategy}.
 */
public class ConstantChainChoiceStrategyTest extends PluginsLoadingTestBase {
    private IResolveDependencyStrategy chainIdStrategy;
    private IMessageProcessor messageProcessorMock;
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
        chainIdStrategy = mock(IResolveDependencyStrategy.class);
        messageProcessorMock = mock(IMessageProcessor.class);
        IMessageProcessingSequence messageProcessingSequenceMock = mock(IMessageProcessingSequence.class);
        IObject args = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"), "{'chain':'chain_to_call_name'}".replace('\'', '"'));

        when(messageProcessorMock.getSequence()).thenReturn(messageProcessingSequenceMock);
        when(messageProcessingSequenceMock.getCurrentReceiverArguments()).thenReturn(args);
        when(chainIdStrategy.resolve(eq("chain_to_call_name"))).thenReturn(id);

        IOC.register(Keys.getOrAdd("chain_id_from_map_name_and_message"), chainIdStrategy);
    }

    @Test
    public void Should_chooseChainUsingIdentifierInCurrentStepArguments()
            throws Exception {
        IChainChoiceStrategy strategy = new ConstantChainChoiceStrategy();

        assertSame(id, strategy.chooseChain(messageProcessorMock));
    }

    @Test(expected = ChainChoiceException.class)
    public void Should_wrapExceptionThrownByIOC()
            throws Exception {
        when(chainIdStrategy.resolve(any())).thenThrow(ResolveDependencyStrategyException.class);

        IChainChoiceStrategy strategy = new ConstantChainChoiceStrategy();

        strategy.chooseChain(messageProcessorMock);
    }
}