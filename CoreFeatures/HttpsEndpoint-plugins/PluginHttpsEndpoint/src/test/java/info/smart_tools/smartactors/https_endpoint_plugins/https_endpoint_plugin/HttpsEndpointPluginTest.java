package info.smart_tools.smartactors.https_endpoint_plugins.https_endpoint_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_by_type_and_name_strategy.ResolveByTypeAndNameStrategy;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class HttpsEndpointPluginTest {

    private IBootstrap bootstrap;
    private HttpsEndpointPlugin plugin;
    private ResolveByTypeAndNameStrategy deserializationStrategyChooser;
    private ResolveByTypeAndNameStrategy resolveCookies;
    private ResolveByTypeAndNameStrategy resolveHeaders;
    private ResolveByTypeAndNameStrategy resolveStatusSetter;
    private ResolveByTypeAndNameStrategy resolveResponseSender;

    @Before
    public void setUp() throws Exception {
        deserializationStrategyChooser = mock(ResolveByTypeAndNameStrategy.class);
        resolveCookies = mock(ResolveByTypeAndNameStrategy.class);
        resolveHeaders = mock(ResolveByTypeAndNameStrategy.class);
        resolveStatusSetter = mock(ResolveByTypeAndNameStrategy.class);
        resolveResponseSender = mock(ResolveByTypeAndNameStrategy.class);
        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope scope = ScopeProvider.getScope(keyOfMainScope);
        scope.setValue(IOC.getIocKey(), new StrategyContainer());
        ScopeProvider.setCurrentScope(scope);
        IOC.register(
                IOC.getKeyForKeyByNameStrategy(),
                new ResolveByNameIocStrategy()
        );
        IOC.register(
                Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new FieldName((String) args[0]);
                            } catch (InvalidArgumentException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                )
        );

        IOC.register(Keys.getKeyByName("ResolveByTypeAndNameStrategy"), new SingletonStrategy(
                        deserializationStrategyChooser
                )
        );
        IOC.register(Keys.getKeyByName("DeserializationStrategyChooser"), new SingletonStrategy(
                        deserializationStrategyChooser
                )
        );
        IOC.register(Keys.getKeyByName("ResponseSenderChooser"), new SingletonStrategy(
                        resolveResponseSender
                )
        );
        bootstrap = mock(IBootstrap.class);
        plugin = new HttpsEndpointPlugin(bootstrap);
        IOC.register(Keys.getKeyByName("CookiesSetterChooser"), new SingletonStrategy(
                        resolveCookies
                )
        );
        IOC.register(Keys.getKeyByName("HeadersExtractorChooser"), new SingletonStrategy(
                        resolveHeaders
                )
        );
        IOC.register(Keys.getKeyByName("ResponseStatusSetter"), new SingletonStrategy(
                        resolveStatusSetter
                )
        );
    }

    @Test
    public void checkPluginCreation()
            throws Exception {
        IBootstrap<IBootstrapItem<String>> bootstrap = mock(IBootstrap.class);
        IPlugin plugin = new HttpsEndpointPlugin(bootstrap);
        assertNotNull(plugin);
        reset(bootstrap);
    }

    @Test
    public void checkLoadExecution()
            throws Exception {
        Checker checker = new Checker();
        checker.item = new BootstrapItem("test");
        IBootstrap<IBootstrapItem<String>> bootstrap = mock(IBootstrap.class);
        List<IBootstrapItem<String>> itemList = new ArrayList<>();
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                checker.item = (IBootstrapItem<String>) args[0];
                itemList.add(checker.item);
                return null;
            }
        })
                .when(bootstrap)
                .add(any(IBootstrapItem.class));
        IPlugin plugin = new HttpsEndpointPlugin(bootstrap);
        plugin.load();
        assertEquals(itemList.size(), 1);
        IBootstrapItem<String> item = itemList.get(0);
        item.executeProcess();
        reset(bootstrap);
    }

    @Test(expected = PluginException.class)
    public void checkPluginExceptionOnPluginLoad()
            throws Exception {
        IBootstrap<IBootstrapItem<String>> bootstrap = mock(IBootstrap.class);
        IPlugin plugin = new HttpsEndpointPlugin(bootstrap);
        doThrow(Exception.class).when(bootstrap).add(any(IBootstrapItem.class));
        plugin.load();
        fail();
    }

}

class Checker {
    public IBootstrapItem<String> item;
}