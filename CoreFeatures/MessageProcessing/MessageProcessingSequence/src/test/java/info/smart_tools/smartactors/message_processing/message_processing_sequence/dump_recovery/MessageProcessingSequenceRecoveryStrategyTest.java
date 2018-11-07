package info.smart_tools.smartactors.message_processing.message_processing_sequence.dump_recovery;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.class_management.interfaces.imodule.IModule;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.irouter.IRouter;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.NestedChainStackOverflowException;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link MessageProcessingSequenceRecoveryStrategy}.
 */
public class MessageProcessingSequenceRecoveryStrategyTest extends PluginsLoadingTestBase {
    private IChainStorage chainStorageMock;
    private IRouter routerMock;
    private IResolveDependencyStrategy chainResolutionStrategyMock;

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
        IOC.register(Keys.getOrAdd("chain_id_from_map_name_and_message"), new IResolveDependencyStrategy() {
            @Override
            public <T> T resolve(Object... args) throws ResolveDependencyStrategyException {
                return (T) String.valueOf(args[0]);
            }
        });

        chainResolutionStrategyMock = mock(IResolveDependencyStrategy.class);
        IOC.register(Keys.getOrAdd(IReceiverChain.class.getCanonicalName()), chainResolutionStrategyMock);

        routerMock = mock(IRouter.class);
        chainStorageMock = mock(IChainStorage.class);

        IOC.register(Keys.getOrAdd(IRouter.class.getCanonicalName()), new SingletonStrategy(routerMock));
        IOC.register(Keys.getOrAdd(IChainStorage.class.getCanonicalName()), new SingletonStrategy(chainStorageMock));
    }

    @Test
    public void Should_recoverSequenceFromDumpRestoringDumpedChains()
            throws Exception {
        IReceiverChain chainA = mock(IReceiverChain.class), chainB = mock(IReceiverChain.class);

        IObject seqDump = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"),
                ("{" +
                        "'maxDepth':4," +
                        "'chainsStack':['a','b','a']," +
                        "'stepsStack':[1,4,2]," +
                        "'scopeRestorationsStack':[false,true,false]," +
                        "'chainsDump':{" +
                        "   'a': {'this-is':'chain-a dump'}" +
                        "}" +
                        "}").replace('\'','"'));

        when(chainStorageMock.resolve("b")).thenReturn(chainB);
        when(chainStorageMock.resolve("a")).thenReturn(chainA);//thenThrow(ChainNotFoundException.class);
        when(chainResolutionStrategyMock.resolve(
                eq("a"),
                same(((IObject) seqDump
                        .getValue(IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "chainsDump")))
                        .getValue(IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "a"))),
                any(),
                same(routerMock)
        )).thenReturn(chainA);
        when(chainA.get(0)).thenReturn(mock(IMessageReceiver.class));
        when(chainA.get(1)).thenReturn(mock(IMessageReceiver.class));
        when(chainA.get(2)).thenReturn(mock(IMessageReceiver.class));
        when(chainA.get(3)).thenReturn(mock(IMessageReceiver.class));
        when(chainB.get(0)).thenReturn(mock(IMessageReceiver.class));
        when(chainB.get(1)).thenReturn(mock(IMessageReceiver.class));
        when(chainB.get(2)).thenReturn(mock(IMessageReceiver.class));
        when(chainB.get(3)).thenReturn(mock(IMessageReceiver.class));
        when(chainB.getScope()).thenReturn(ScopeProvider.getCurrentScope());
        when(chainA.getScope()).thenReturn(ScopeProvider.getCurrentScope());
        when(chainB.getModule()).thenReturn(mock(IModule.class));
        when(chainA.getName()).thenReturn("a");
        when(chainB.getName()).thenReturn("b");

        IMessageProcessingSequence sequence = new MessageProcessingSequenceRecoveryStrategy().resolve(seqDump, null);

        assertNotNull(sequence);

        assertEquals(2, sequence.getCurrentLevel());
        assertEquals(3, sequence.getStepAtLevel(2));
        assertEquals(4, sequence.getStepAtLevel(1));
        assertEquals(1, sequence.getStepAtLevel(0));

        sequence.setScopeSwitchingChainName("a");
        sequence.callChain("a");
        try {
            sequence.callChain("a");
            fail();
        } catch (NestedChainStackOverflowException ok) {}
    }
}
