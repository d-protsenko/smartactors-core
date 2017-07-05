package info.smart_tools.smartactors.message_processing.chain_modifications;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test for {@link ReplaceReceiversChainModificationStrategy}.
 */
public class ReplaceReceiversChainModificationStrategyTest extends PluginsLoadingTestBase {
    private IResolveDependencyStrategy replaceStrategy1;
    private IResolveDependencyStrategy replaceStrategy2;
    private IReceiverChain originalChainMock;

    private IMessageReceiver[] receivers = new IMessageReceiver[] {
            mock(IMessageReceiver.class), mock(IMessageReceiver.class),
            mock(IMessageReceiver.class), mock(IMessageReceiver.class),
            mock(IMessageReceiver.class), mock(IMessageReceiver.class)};

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
        replaceStrategy1 = mock(IResolveDependencyStrategy.class);
        IOC.register(Keys.getOrAdd("replacement 1 strategy"), replaceStrategy1);
        replaceStrategy2 = mock(IResolveDependencyStrategy.class);
        IOC.register(Keys.getOrAdd("replacement 2 strategy"), replaceStrategy2);
        originalChainMock = mock(IReceiverChain.class);
    }

    @Test
    public void Should_forwardCallsToOriginalChain()
            throws Exception {
        when(originalChainMock.get(0)).thenReturn(receivers[0]);

        IReceiverChain decorated = new ReplaceReceiversChainModificationStrategy().resolve(originalChainMock,
                IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"), "{'replacements':[]}".replace('\'','"')));

        assertSame(receivers[0], decorated.get(0));

        reset(originalChainMock);

        decorated.getArguments(1);
        verify(originalChainMock).getArguments(1);
        reset(originalChainMock);

        decorated.getName();
        verify(originalChainMock).getName();
        reset(originalChainMock);

        decorated.getExceptionalChainAndEnvironments(null);
        verify(originalChainMock).getExceptionalChainAndEnvironments(null);
        reset(originalChainMock);

        decorated.getExceptionalChains();
        verify(originalChainMock).getExceptionalChains();
        reset(originalChainMock);

        decorated.getChainDescription();
        verify(originalChainMock).getChainDescription();
    }

    @Test
    public void Should_replaceReceivers()
            throws Exception {
        IObject modDesc = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"),
                ("{'replacements':[" +
                        "{" +
                        "'step':0," +
                        "'dependency':'replacement 1 strategy'," +
                        "'args':'args1'" +
                        "}," +
                        "{" +
                        "'step':3," +
                        "'dependency':'replacement 2 strategy'," +
                        "'args':'42'" +
                        "}]}").replace('\'','"'));

        when(originalChainMock.get(0)).thenReturn(receivers[0]);
        when(originalChainMock.get(1)).thenReturn(receivers[1]);
        when(originalChainMock.get(2)).thenReturn(receivers[2]);

        when(replaceStrategy1.resolve(same(receivers[0]), eq("args1")))
                .thenReturn(receivers[3])
                .thenThrow(ResolveDependencyStrategyException.class);

        when(replaceStrategy2.resolve(same(null), eq("42")))
                .thenReturn(receivers[4])
                .thenThrow(ResolveDependencyStrategyException.class);

        IReceiverChain decorated = new ReplaceReceiversChainModificationStrategy().resolve(originalChainMock, modDesc);

        assertSame(receivers[3], decorated.get(0));
        assertSame(receivers[1], decorated.get(1));
        assertSame(receivers[2], decorated.get(2));
        assertSame(receivers[4], decorated.get(3));
        assertNull(decorated.get(4));
    }

    @Test(expected = ResolveDependencyStrategyException.class)
    public void Should_throwWhenReplacementStepIndexIsTooLarge()
            throws Exception {
        IObject modDesc = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"),
                ("{'replacements':[" +
                        "{" +
                        "'step':2," +
                        "'dependency':'replacement 1 strategy'," +
                        "'args':'args1'" +
                        "}]}").replace('\'','"'));

        when(originalChainMock.get(0)).thenReturn(receivers[0]);

        assertNotNull(new ReplaceReceiversChainModificationStrategy().resolve(originalChainMock, modDesc));
    }
}
