package info.smart_tools.smartactors.testing.test_reporter;

import info.smart_tools.smartactors.field_plugins.ifield_plugin.IFieldPlugin;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import info.smart_tools.smartactors.testing.interfaces.itest_reporter.ITestReporter;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class TestReportTest extends PluginsLoadingTestBase {
    @Override
    protected void loadPlugins() throws Exception {
        load(ScopedIOCPlugin.class);
        load(PluginScopeProvider.class);
        load(PluginIOCKeys.class);
        load(IFieldNamePlugin.class);
        load(IFieldPlugin.class);
        load(PluginDSObject.class);
    }

    @Test
    public void Should_AddTestCases() throws Exception {
        ITestReporter reporter = new TestReporter("TestReportTest");
        IObject testInfo = buildTestInfo();
        reporter.beforeTest(testInfo);
        // SOME STUFF IN REAL CODE IS HERE
        reporter.afterTest(null);
        // NEXT TEST
        reporter.beforeTest(testInfo);
        // SOME STUFF IN REAL CODE IS HERE
        reporter.afterTest(new Exception("It should fall"));

        reporter.make();
    }

    private IObject buildTestInfo() {
        IObject testInfo = mock(IObject.class);
        return testInfo;
    }
}
