package info.smart_tools.smartactors.message_processing.condition_chain_choice_strategy;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
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

public class ConditionChainChoiceStrategyTest extends PluginsLoadingTestBase {
    private IResolveDependencyStrategy chainIdStrategy;
    private IMessageProcessor messageProcessorMock;
    private Object trueId = new Object();
    private Object falseId = new Object();

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
        IObject args = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"), "{'trueChain':'trueChainId', 'falseChain':'falseChainId'}".replace('\'', '"'));

        when(messageProcessorMock.getSequence()).thenReturn(messageProcessingSequenceMock);
        when(messageProcessingSequenceMock.getCurrentReceiverArguments()).thenReturn(args);

        when(chainIdStrategy.resolve(eq("trueChainId"))).thenReturn(trueId);
        when(chainIdStrategy.resolve(eq("falseChainId"))).thenReturn(falseId);

        IOC.register(Keys.getKeyByName("chain_id_from_map_name_and_message"), chainIdStrategy);
    }

    @Test
    public void Should_chooseChainConditionIsTrue()
            throws Exception {
        IObject messageArgs = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"), "{'chainCondition': true}".replace('\'', '"'));
        when(messageProcessorMock.getMessage()).thenReturn(messageArgs);
        IChainChoiceStrategy strategy = new ConditionChainChoiceStrategy();

        assertSame(trueId, strategy.chooseChain(messageProcessorMock));
    }

    @Test
    public void Should_chooseChainConditionIsFalse()
            throws Exception {
        IObject messageArgs = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"), "{'chainCondition': false}".replace('\'', '"'));
        when(messageProcessorMock.getMessage()).thenReturn(messageArgs);
        IChainChoiceStrategy strategy = new ConditionChainChoiceStrategy();

        assertSame(falseId, strategy.chooseChain(messageProcessorMock));
    }



    @Test(expected = ChainChoiceException.class)
    public void Should_wrapExceptionThrownByIOC()
            throws Exception {
        when(chainIdStrategy.resolve(any())).thenThrow(ResolveDependencyStrategyException.class);

        IChainChoiceStrategy strategy = new ConditionChainChoiceStrategy();

        strategy.chooseChain(messageProcessorMock);
    }
}
