package info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin;

import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap.Bootstrap;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.exception.ProcessExecutionException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.helpers.IOCInitializer.IOCInitializer;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class IFieldNamePluginTest extends IOCInitializer {

    private IFieldNamePlugin plugin;
    private IBootstrap bootstrap;

    @Override
    protected void registry(String... strategyNames) throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        bootstrap = new Bootstrap();
        bootstrap.add(new BootstrapItem("IOC").process(()->{}));
        plugin = new IFieldNamePlugin(bootstrap);
    }

    @Test
    public void ShouldCorrectLoadAndRevertPlugin() throws Exception {
        IOC.register(
                Keys.getKeyByName("bootstrap item"),
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> new BootstrapItem((String) args[0])
                )
        );
        plugin.load();
        bootstrap.start();
        IKey fieldNameKey = Keys.getKeyByName(IFieldName.class.getCanonicalName());

        Object a = IOC.resolve(fieldNameKey, "name");
        assertNotNull(a);
        bootstrap.revert();
        try {
            Object a1 = IOC.resolve(fieldNameKey, "name");
            fail();
        } catch (ResolutionException e) {/**/}
    }

    @Test(expected = PluginException.class)
    public void ShouldThrowPluginException_When_InternalErrorIsOccurred() throws Exception {
        plugin.load();
        bootstrap.start();
    }

    @Test(expected = ProcessExecutionException.class)
    public void ShouldThrowException_When_ExceptionInLambdaIsThrown() throws Exception {
        IOC.register(
                Keys.getKeyByName("bootstrap item"),
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> new BootstrapItem((String) args[0])
                )
        );
        plugin.load();
        ScopeProvider.setCurrentScope(null);
        bootstrap.start();
    }
}