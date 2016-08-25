package info.smart_tools.smartactors.test.test_assertion;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.plugin.dsobject.PluginDSObject;
import info.smart_tools.smartactors.plugin.ifieldname.IFieldNamePlugin;
import info.smart_tools.smartactors.plugin.ioc_keys.PluginIOCKeys;
import info.smart_tools.smartactors.plugin.scope_provider.PluginScopeProvider;
import info.smart_tools.smartactors.plugin.scoped_ioc.ScopedIOCPlugin;
import info.smart_tools.smartactors.test.iassertion.IAssertion;
import info.smart_tools.smartactors.testing.helpers.plugins_loading_test_base.PluginsLoadingTestBase;

/**
 * Base class for tests for {@link IAssertion} implementations.
 */
public class AssertionTestBase extends PluginsLoadingTestBase {
    @Override
    protected void loadPlugins() throws Exception {
        load(ScopedIOCPlugin.class);
        load(PluginScopeProvider.class);
        load(PluginIOCKeys.class);
        load(PluginDSObject.class);
        load(IFieldNamePlugin.class);
    }

    protected void apply(Class<? extends IAssertion> clz, IObject desc, Object value)
            throws Exception {
        (clz.newInstance()).check(desc, value);
    }

    protected void apply(Class<? extends IAssertion> clz, String desc, Object value)
            throws Exception {
        apply(clz, IOC.<IObject>resolve(Keys.getOrAdd(IObject.class.getCanonicalName()), desc), value);
    }
}
