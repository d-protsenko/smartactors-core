package info.smart_tools.smartactors.debugger.session_impl;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.debugger.interfaces.IDebuggerBreakpointsStorage;
import info.smart_tools.smartactors.debugger.interfaces.IDebuggerSequence;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link DebuggerBreakpointsStorageImpl}.
 */
public class DebuggerBreakpointsStorageImplTest extends PluginsLoadingTestBase {
    private IChainStorage chainStorageMock;
    private IReceiverChain chainMock;
    private IObject stepArgsMock1, stepArgsMock2;
    private IDebuggerSequence debuggerSequenceMock;
    private IMessageProcessingSequence realSequenceMock;

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
        chainStorageMock = mock(IChainStorage.class);

        IOC.register(Keys.resolveByName(IChainStorage.class.getCanonicalName()), new SingletonStrategy(chainStorageMock));

        debuggerSequenceMock = mock(IDebuggerSequence.class);
        realSequenceMock = mock(IMessageProcessingSequence.class);

        when(debuggerSequenceMock.getRealSequence()).thenReturn(realSequenceMock);

        chainMock = mock(IReceiverChain.class);
        stepArgsMock1 = mock(IObject.class);
        stepArgsMock2 = mock(IObject.class);

        IOC.register(Keys.resolveByName("chain_id_from_map_name"), new IResolutionStrategy() {
            @Override
            public <T> T resolve(Object... args) throws ResolutionStrategyException {
                return (T) args[0];
            }
        });
    }

    @Test
    public void Should_createBreakpoints()
            throws Exception {
        when(chainStorageMock.resolve("thatChain")).thenReturn(chainMock);
        when(chainMock.getArguments(42)).thenReturn(stepArgsMock1);
        when(chainMock.getArguments(21)).thenReturn(stepArgsMock2);

        IDebuggerBreakpointsStorage storage = new DebuggerBreakpointsStorageImpl();

        String id = storage.addBreakpoint(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.iobject.IObject"),
                ("{" +
                        "'chain':'thatChain'," +
                        "'step':42," +
                        "'enabled':true" +
                        "}").replace('\'','"')));

        when(realSequenceMock.getCurrentReceiverArguments()).thenReturn(stepArgsMock1);
        assertTrue(storage.shouldBreakAt(debuggerSequenceMock));

        when(realSequenceMock.getCurrentReceiverArguments()).thenReturn(stepArgsMock2);
        assertFalse(storage.shouldBreakAt(debuggerSequenceMock));

        assertEquals(1, storage.listBreakpoints().size());

        String id2 = storage.addBreakpoint(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.iobject.IObject"),
                ("{" +
                        "'chain':'thatChain'," +
                        "'step':21," +
                        "'enabled':false" +
                        "}").replace('\'','"')));

        assertEquals(2, storage.listBreakpoints().size());

        when(realSequenceMock.getCurrentReceiverArguments()).thenReturn(stepArgsMock2);
        assertFalse(storage.shouldBreakAt(debuggerSequenceMock));

        storage.modifyBreakpoint(id2, IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.iobject.IObject"),
                ("{" +
                        "'id':'" + id2 + "'," +
                        "'enabled':true" +
                        "}").replace('\'','"')));

        when(realSequenceMock.getCurrentReceiverArguments()).thenReturn(stepArgsMock2);
        assertTrue(storage.shouldBreakAt(debuggerSequenceMock));
    }
}
