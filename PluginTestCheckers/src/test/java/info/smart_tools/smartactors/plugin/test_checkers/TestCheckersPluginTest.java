package info.smart_tools.smartactors.plugin.test_checkers;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.configuration_object.ConfigurationObject;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.core.irouter.IRouter;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.test.iassertion.IAssertion;
import info.smart_tools.smartactors.test.iresult_checker.IResultChecker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestCheckersPluginTest {

    @Before
    public void init()
            throws Exception {
        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope scope = ScopeProvider.getScope(keyOfMainScope);
        scope.setValue(IOC.getIocKey(), new StrategyContainer());
        ScopeProvider.setCurrentScope(scope);

        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            try {
                                return new Key((String) a[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            try {
                                return new FieldName((String) a[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.getCanonicalName()),
                new ApplyFunctionToArgumentsStrategy(
                        (a) -> {
                            try {
                                return new DSObject();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );
        IOC.register(
                IOC.resolve(
                        IOC.getKeyForKeyStorage(), "configuration object"
                ),
                new ApplyFunctionToArgumentsStrategy(
                        (a) -> {

                            if (a.length == 0) {
                                return new ConfigurationObject();
                            } else if (a.length == 1 && a[0] instanceof String) {
                                try {
                                    return new ConfigurationObject((String) a[0]);
                                } catch (InvalidArgumentException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                throw new RuntimeException("Could not create new instance of Configuration Object.");
                            }
                        }
                )
        );
        IAssertion assertionMock = mock(IAssertion.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), "assertion of type myType"), new SingletonStrategy(assertionMock)
        );
        Object receiverIdStrategyMock = mock(Object.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), "receiver_id_from_iobject"), new SingletonStrategy(receiverIdStrategyMock)
        );
        IRouter routerMock = mock(IRouter.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IRouter.class.getCanonicalName()), new IResolveDependencyStrategy() {
            @Override
            public <T> T resolve(Object... args) throws ResolveDependencyStrategyException {
                return (T) routerMock;
            }
        });
    }

    @Test
    public void checkPluginCreation()
            throws Exception {
        IBootstrap<IBootstrapItem<String>> bootstrap = mock(IBootstrap.class);
        IPlugin plugin = new TestCheckersPlugin(bootstrap);
        assertNotNull(plugin);
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnCreation()
            throws Exception {
        new TestCheckersPlugin(null);
        fail();
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
        IPlugin plugin = new TestCheckersPlugin(bootstrap);
        plugin.load();
        assertEquals(itemList.size(), 1);
        IBootstrapItem<String> item = itemList.get(0);
        item.executeProcess();
        List<IObject> assertions = new ArrayList<>();
        IObject assertion = mock(IObject.class);
        assertions.add(assertion);
        when(assertion.getValue(new FieldName("name"))).thenReturn("myName");
        when(assertion.getValue(new FieldName("type"))).thenReturn("myType");
        when(assertion.getValue(new FieldName("value"))).thenReturn("myValue");
        IObject interception = mock(IObject.class);
        when(interception.getValue(new FieldName("class"))).thenReturn(Integer.class.getCanonicalName());
        IResultChecker assertChecker = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyStorage(), IResultChecker.class.getCanonicalName() + "#assert"), assertions);
        IResultChecker interceptChecker = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyStorage(), IResultChecker.class.getCanonicalName() + "#intercept"), interception);
        assertNotNull(assertChecker);
        assertNotNull(interceptChecker);
    }

    @Test (expected = PluginException.class)
    public void checkPluginExceptionOnPluginLoad()
            throws Exception {
        IBootstrap<IBootstrapItem<String>> bootstrap = mock(IBootstrap.class);
        IPlugin plugin = new TestCheckersPlugin(bootstrap);
        doThrow(Exception.class).when(bootstrap).add(any(IBootstrapItem.class));
        plugin.load();
        fail();
    }
}
