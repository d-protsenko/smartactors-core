package info.smart_tools.smartactors.message_processing.chain_storage;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.exceptions.ChainCreationException;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.message_processing_interfaces.irouter.IRouter;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Test for {@link ChainStorage}.
 */
public class ChainStorageTest extends PluginsLoadingTestBase {
    private IResolveDependencyStrategy receiverChainStrategyMock;

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
        receiverChainStrategyMock = mock(IResolveDependencyStrategy.class);

        IOC.register(Keys.getOrAdd(IReceiverChain.class.getCanonicalName()), receiverChainStrategyMock);
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
    public void Should_returnChain_When_chainIsFound()
            throws Exception {
        Map mapMock = mock(Map.class);
        IRouter routerMock = mock(IRouter.class);
        IReceiverChain chainMock = mock(IReceiverChain.class);
        Object idMock = mock(Object.class);

        when(mapMock.get(same(idMock))).thenReturn(chainMock);

        IChainStorage storage = new ChainStorage(mapMock, routerMock);

        assertSame(chainMock, storage.resolve(idMock));
    }

    @Test
    public void Should_resolveAndStoreChains()
            throws Exception {
        IKey receiverChainKey = mock(IKey.class);

        IRouter routerMock = mock(IRouter.class);
        Map mapMock = mock(Map.class);
        IReceiverChain receiverChainMock = mock(IReceiverChain.class);
        Object chainId = mock(Object.class);
        IObject chainDesc = mock(IObject.class);

        ChainStorage chainStorage = new ChainStorage(mapMock, routerMock);

        when(receiverChainStrategyMock.resolve(same(chainId), same(chainDesc), same(chainStorage), same(routerMock)))
                .thenReturn(receiverChainMock);

        chainStorage.register(chainId, chainDesc);

        Mockito.verify(mapMock).put(chainId, receiverChainMock);
        reset(mapMock);

        when(mapMock.put(chainId, receiverChainMock)).thenReturn(mock(IReceiverChain.class));
        when(receiverChainStrategyMock.resolve(same(chainId), same(chainDesc), same(chainStorage), same(routerMock)))
                .thenReturn(receiverChainMock);

        chainStorage.register(chainId, chainDesc);

        Mockito.verify(mapMock).put(chainId, receiverChainMock);
    }

    @Test
    public void Should_wrapExceptionsThrownByIOC()
            throws Exception {
        ResolveDependencyStrategyException resolutionException = mock(ResolveDependencyStrategyException.class);
        IRouter routerMock = mock(IRouter.class);
        Map mapMock = mock(Map.class);
        Object chainId = mock(Object.class);
        IObject chainDesc = mock(IObject.class);

        ChainStorage chainStorage = new ChainStorage(mapMock, routerMock);

        when(receiverChainStrategyMock.resolve(any(), any(), any(), any())).thenThrow(resolutionException);

        try {
            chainStorage.register(chainId, chainDesc);
            fail();
        } catch (ChainCreationException e) {
            assertSame(resolutionException, e.getCause().getCause());
        }
    }

    @Test
    public void Should_enumerate_returnListOfAllChainIdentifiers()
            throws Exception {
        IRouter routerMock = mock(IRouter.class);
        Map<Object, IReceiverChain> map = new HashMap<>();
        Object key1 = mock(Object.class), key2 = mock(Object.class);
        IReceiverChain chainMock = mock(IReceiverChain.class);

        map.put(key1, chainMock);
        map.put(key2, chainMock);

        ChainStorage chainStorage = new ChainStorage(map, routerMock);

        assertEquals(new ArrayList<Object>(map.keySet()), chainStorage.enumerate());
    }

    @Test
    public void Should_updateChain()
            throws Exception {
        IResolveDependencyStrategy modStrategy = mock(IResolveDependencyStrategy.class);
        IObject modDesc = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{'modification':'some cool chain upgrade'}".replace('\'','"'));
        IReceiverChain originalChain = mock(IReceiverChain.class);
        IReceiverChain modifiedChain = mock(IReceiverChain.class);
        Object chainId = new Object();

        IOC.register(Keys.getOrAdd("some cool chain upgrade"), modStrategy);

        Map mapMock = mock(Map.class);

        when(mapMock.get(same(chainId))).thenReturn(originalChain);
        when(modStrategy.resolve(same(originalChain), same(modDesc))).thenReturn(modifiedChain);

        IChainStorage storage = new ChainStorage(mapMock, mock(IRouter.class));

        storage.update(chainId, modDesc);

        verify(mapMock).put(same(chainId), same(modifiedChain));
    }
}
