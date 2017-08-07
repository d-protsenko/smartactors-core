package info.smart_tools.smartactors.message_processing.chain_storage.impl;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.exceptions.ChainModificationException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;

import java.text.MessageFormat;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link ChainStateImpl}.
 */
public class ChainStateImplTest extends PluginsLoadingTestBase {
    private IReceiverChain[] chainMocks;
    private IResolveDependencyStrategy[] modStrategyMocks;
    private IObject[] modArgMocks;

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
        chainMocks = new IReceiverChain[4];

        for (int i = 0; i < chainMocks.length; i++) {
            chainMocks[i] = mock(IReceiverChain.class);
        }

        modStrategyMocks = new IResolveDependencyStrategy[2];
        modArgMocks = new IObject[modStrategyMocks.length];

        for (int i = 0; i < modStrategyMocks.length; i++) {
            modStrategyMocks[i] = mock(IResolveDependencyStrategy.class);
            modArgMocks[i] = mock(IObject.class);

            String mk = MessageFormat.format("mod-{0}", i);
            IOC.register(Keys.getOrAdd(mk), modStrategyMocks[i]);
            when(modArgMocks[i].getValue(IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "modification")))
                    .thenReturn(mk);
        }

        when(modStrategyMocks[0].resolve(chainMocks[0], modArgMocks[0])).thenReturn(chainMocks[3]);
        when(modStrategyMocks[0].resolve(chainMocks[1], modArgMocks[0])).thenReturn(chainMocks[2]);
        when(modStrategyMocks[1].resolve(chainMocks[0], modArgMocks[1])).thenReturn(chainMocks[1]);
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrowWhenInitialChainIsNull()
            throws Exception {
        new ChainStateImpl(null);
    }

    @Test(expected = ChainModificationException.class)
    public void Should_throwWhenTryingToRollbackNonExistModification()
            throws Exception {
        new ChainStateImpl(chainMocks[0]).rollback(new Object());
    }

    @Test
    public void Should_updateChainsAndRollbackUpdates()
            throws Exception {
        ChainStateImpl state = new ChainStateImpl(chainMocks[0]);

        assertSame(chainMocks[0], state.getCurrent());

        Object mId1 = state.update(modArgMocks[1]);

        assertSame(chainMocks[1], state.getCurrent());

        Object mId2 = state.update(modArgMocks[0]);

        assertSame(chainMocks[2], state.getCurrent());

        state.rollback(mId1);

        assertSame(chainMocks[3], state.getCurrent());

        state.rollback(mId2);

        assertSame(chainMocks[0], state.getCurrent());
    }
}
