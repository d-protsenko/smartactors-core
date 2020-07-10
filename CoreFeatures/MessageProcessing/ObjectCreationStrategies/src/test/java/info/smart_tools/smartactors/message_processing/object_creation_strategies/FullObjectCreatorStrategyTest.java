package info.smart_tools.smartactors.message_processing.object_creation_strategies;

import info.smart_tools.smartactors.base.interfaces.istrategy.IStrategy;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_processing_interfaces.object_creation_interfaces.IReceiverObjectCreator;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link FullObjectCreatorStrategy}.
 */
public class FullObjectCreatorStrategyTest extends PluginsLoadingTestBase {
    private IStrategy creator1ResolutionStrategyMock;
    private IStrategy creator2ResolutionStrategyMock;
    private IReceiverObjectCreator creator1Mock;
    private IReceiverObjectCreator creator2Mock;

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
        creator1ResolutionStrategyMock = mock(IStrategy.class);
        creator2ResolutionStrategyMock = mock(IStrategy.class);
        creator1Mock = mock(IReceiverObjectCreator.class);
        creator2Mock = mock(IReceiverObjectCreator.class);

        when(creator1ResolutionStrategyMock.resolve(isNull(), any(), any())).thenReturn(creator1Mock);
        when(creator2ResolutionStrategyMock.resolve(same(creator1Mock), any(), any())).thenReturn(creator2Mock);

        IOC.register(Keys.getKeyByName("filter 1 dependency"), creator1ResolutionStrategyMock);
        IOC.register(Keys.getKeyByName("filter 2 dependency"), creator2ResolutionStrategyMock);
    }

    @Test
    public void Should_resolveCreatorsPipeline()
            throws Exception {
        IObject objectConfig = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"),
                ("{" +
                 "  'filters':[" +
                 "      {" +
                        "   'dependency':'filter 1 dependency'" +
                 "      }," +
                        "{" +
                        "   'dependency':'filter 2 dependency'" +
                        "}" +
                 "  ]" +
                 "}").replace('\'','"'));

        IReceiverObjectCreator result = new FullObjectCreatorStrategy().resolve(objectConfig);

        assertSame(creator2Mock, result);
    }
}
