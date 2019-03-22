package info.smart_tools.smartactors.checkpoint.recover_strategies;

import info.smart_tools.smartactors.checkpoint.interfaces.exceptions.RecoverStrategyInitializationException;
import info.smart_tools.smartactors.checkpoint.recover_strategies.chain_choice.ChainSequenceRecoverStrategy;
import info.smart_tools.smartactors.checkpoint.recover_strategies.chain_choice.IRecoveryChainChoiceStrategy;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link ChainSequenceRecoverStrategy}.
 */
public class ChainSequenceRecoverStrategyTest extends PluginsLoadingTestBase {

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
        /*
        IOC.register(Keys.getKeyByName("chain_id_from_map_name_and_message"), new IStrategy() {
            @Override
            public <T> T resolve(Object... args) throws StrategyException {
                return (T) args[0].toString().concat("__1");
            }
        });
        */
    }

    @Test
    public void Should_chooseChainInConfiguredSequence()
            throws Exception {
        IObject args = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"),
                "{'trials':[1,3,2],'chains':['A','B','C','D']}".replace('\'','"'));
        IObject state = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"));

        IRecoveryChainChoiceStrategy strategy = new ChainSequenceRecoverStrategy();

        strategy.init(state, args);

        assertEquals("A", strategy.chooseRecoveryChain(state));
        assertEquals("B", strategy.chooseRecoveryChain(state));
        assertEquals("B", strategy.chooseRecoveryChain(state));
        assertEquals("B", strategy.chooseRecoveryChain(state));
        assertEquals("C", strategy.chooseRecoveryChain(state));
        assertEquals("C", strategy.chooseRecoveryChain(state));
        assertEquals("D", strategy.chooseRecoveryChain(state));
        assertEquals("D", strategy.chooseRecoveryChain(state));
        assertEquals("D", strategy.chooseRecoveryChain(state));
        assertEquals("D", strategy.chooseRecoveryChain(state));
        assertEquals("D", strategy.chooseRecoveryChain(state));
        assertEquals("D", strategy.chooseRecoveryChain(state));
    }

    @Test(expected = RecoverStrategyInitializationException.class)
    public void Should_throwWhenArgumentsContainNoRequiredFields()
            throws Exception {
        IObject args = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"));
        IObject state = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"));

        IRecoveryChainChoiceStrategy strategy = new ChainSequenceRecoverStrategy();

        strategy.init(state, args);
    }

    @Test(expected = RecoverStrategyInitializationException.class)
    public void Should_throwWhenListSizesDoNotMatch()
            throws Exception {
        IObject args = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"),
                "{'trials':[1,3,2],'chains':['A','B','C','D','E']}".replace('\'', '"'));
        IObject state = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"));

        IRecoveryChainChoiceStrategy strategy = new ChainSequenceRecoverStrategy();

        strategy.init(state, args);
    }
}
