package info.smart_tools.smartactors.message_processing.chain_storage;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_processing.chain_storage.interfaces.IChainState;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.irouter.IRouter;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test for {@link ChainStorage}.
 */
public class ChainStorageTest extends PluginsLoadingTestBase {
    private IResolveDependencyStrategy receiverChainStrategyMock;
    private IResolveDependencyStrategy chainStateStrategyMock;
    private IRouter routerMock;

    private IChainState[] stateMocks;
    private IReceiverChain[] chianMocks;
    private IObject[] descs;

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
        routerMock = mock(IRouter.class);

        receiverChainStrategyMock = mock(IResolveDependencyStrategy.class);
        chainStateStrategyMock = mock(IResolveDependencyStrategy.class);

        IOC.register(Keys.getKeyByName(IReceiverChain.class.getCanonicalName()), receiverChainStrategyMock);
        IOC.register(Keys.getKeyByName(IChainState.class.getCanonicalName()), chainStateStrategyMock);

        stateMocks = new IChainState[] {mock(IChainState.class), mock(IChainState.class), mock(IChainState.class)};
        chianMocks = new IReceiverChain[] {mock(IReceiverChain.class), mock(IReceiverChain.class), mock(IReceiverChain.class)};
        descs = new IObject[] {mock(IObject.class), mock(IObject.class), mock(IObject.class)};
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_chainsMapIsNull()
            throws Exception {
        assertNull(new ChainStorage(null, mock(IRouter.class)));
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_routerIsNull()
            throws Exception {
        assertNull(new ChainStorage(mock(Map.class), null));
    }

    @Test(expected = ChainNotFoundException.class)
    public void Should_throw_When_chainIsNotFound()
            throws Exception {
        Map mapMock = mock(Map.class);
        IRouter routerMock = mock(IRouter.class);

        IChainStorage storage = new ChainStorage(mapMock, routerMock);

        storage.resolve(mock(Object.class));
    }

    @Test
    public void Should_registerAndResolveChains()
            throws Exception {
        IChainStorage storage = new ChainStorage(new HashMap<>(), routerMock);

        when(receiverChainStrategyMock.resolve("the_chain", descs[0], storage, routerMock)).thenReturn(chianMocks[0]);
        when(chainStateStrategyMock.resolve(chianMocks[0])).thenReturn(stateMocks[0]);

        storage.register("the_chain", descs[0]);

        when(stateMocks[0].getCurrent()).thenReturn(chianMocks[0]);

        assertSame(chianMocks[0], storage.resolve("the_chain"));

        when(receiverChainStrategyMock.resolve("the_chain", descs[1], storage, routerMock)).thenReturn(chianMocks[1]);
        when(chainStateStrategyMock.resolve(chianMocks[1])).thenReturn(stateMocks[1]);

        storage.register("the_chain", descs[1]);

        when(stateMocks[1].getCurrent()).thenReturn(chianMocks[1]);

        assertSame(chianMocks[1], storage.resolve("the_chain"));
    }

    @Test
    public void Should_enumerateRegisteredChains()
            throws Exception {
        IChainStorage storage = new ChainStorage(new HashMap<>(), routerMock);

        when(receiverChainStrategyMock.resolve("the_chain1", descs[0], storage, routerMock)).thenReturn(chianMocks[0]);
        when(chainStateStrategyMock.resolve(chianMocks[0])).thenReturn(stateMocks[0]);
        when(receiverChainStrategyMock.resolve("the_chain2", descs[1], storage, routerMock)).thenReturn(chianMocks[1]);
        when(chainStateStrategyMock.resolve(chianMocks[1])).thenReturn(stateMocks[1]);

        storage.register("the_chain1", descs[0]);
        storage.register("the_chain2", descs[1]);

        List<Object> enumResult = storage.enumerate();

        assertEquals(2, enumResult.size());
        assertTrue(enumResult.contains("the_chain1"));
        assertTrue(enumResult.contains("the_chain2"));
    }

    @Test
    public void Should_updateAndRollbackChainStates()
            throws Exception {
        IChainStorage storage = new ChainStorage(new HashMap<>(), routerMock);

        when(receiverChainStrategyMock.resolve("the_chain", descs[0], storage, routerMock)).thenReturn(chianMocks[0]);
        when(chainStateStrategyMock.resolve(chianMocks[0])).thenReturn(stateMocks[0]);

        storage.register("the_chain", descs[0]);

        Object mId = new Object();
        when(stateMocks[0].update(descs[1])).thenReturn(mId);

        assertSame(mId, storage.update("the_chain", descs[1]));

        verify(stateMocks[0]).update(descs[1]);

        storage.rollback("the_chain", mId);

        verify(stateMocks[0]).rollback(mId);
    }
}
