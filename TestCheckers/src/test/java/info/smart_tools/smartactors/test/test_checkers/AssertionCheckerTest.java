package info.smart_tools.smartactors.test.test_checkers;

import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.base.exception.initialization_exception.InitializationException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.plugin.configuration_object.InitializeConfigurationObjectStrategies;
import info.smart_tools.smartactors.plugin.dsobject.PluginDSObject;
import info.smart_tools.smartactors.plugin.ifieldname.IFieldNamePlugin;
import info.smart_tools.smartactors.plugin.ioc_keys.PluginIOCKeys;
import info.smart_tools.smartactors.plugin.scope_provider.PluginScopeProvider;
import info.smart_tools.smartactors.plugin.scoped_ioc.ScopedIOCPlugin;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.test.iassertion.IAssertion;
import info.smart_tools.smartactors.test.iassertion.exception.AssertionFailureException;
import info.smart_tools.smartactors.testing.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for {@link AssertionChecker}.
 */
public class AssertionCheckerTest extends PluginsLoadingTestBase {
    private IAssertion assertion1Mock;
    private IAssertion assertion2Mock;
    private IMessageProcessor messageProcessorMock;
    private IObject environmentMock;

    @Override
    protected void loadPlugins() throws Exception {
        load(ScopedIOCPlugin.class);
        load(PluginScopeProvider.class);
        load(PluginIOCKeys.class);
        load(PluginDSObject.class);
        load(IFieldNamePlugin.class);
        load(InitializeConfigurationObjectStrategies.class);
    }

    @Override
    protected void registerMocks()
            throws Exception {
        assertion1Mock = Mockito.mock(IAssertion.class);
        assertion2Mock = Mockito.mock(IAssertion.class);
        messageProcessorMock = Mockito.mock(IMessageProcessor.class);
        environmentMock = Mockito.mock(IObject.class);

        Mockito.when(messageProcessorMock.getEnvironment()).thenReturn(environmentMock);

        IOC.register(Keys.getOrAdd("assertion of type atype1"), new SingletonStrategy(assertion1Mock));
        IOC.register(Keys.getOrAdd("assertion of type atype2"), new SingletonStrategy(assertion2Mock));
    }

    @Test(expected = InitializationException.class)
    public void Should_constructorThrowWhenCannotResolveAssertionDependency()
            throws Exception {
        IObject a1desc = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{'type': 'nonexist', 'name': 'Nope'}".replace('\'', '"'));

        new AssertionChecker(Collections.singletonList(a1desc));
    }

    @Test(expected = InitializationException.class)
    public void Should_constructorThrowWhenCannotResolveFieldNameDependency()
            throws Exception {
        IObject a1desc = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{'type': 'atype1', 'name': 'Nope'}".replace('\'', '"'));

        IOC.remove(Keys.getOrAdd(IFieldName.class.getCanonicalName()));

        new AssertionChecker(Collections.singletonList(a1desc));
    }

    @Test(expected = InitializationException.class)
    public void Should_constructorThrowWhenCannotResolveIObjectDependency()
            throws Exception {
        IObject a1desc = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{'type': 'atype1', 'name': 'Nope'}".replace('\'', '"'));

        IResolveDependencyStrategy strategy = new ApplyFunctionToArgumentsStrategy(
                (a) -> {
                    try {
                        String name = (String) a[0];
                        if (name.equals("type")) {
                            throw new Exception();
                        }
                        return new FieldName(name);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                strategy
        );

        new AssertionChecker(Collections.singletonList(a1desc));
    }

    @Test
    public void Should_checkAssertions()
            throws Exception {
        IObject a1desc = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{'type': 'atype1', 'name': 'Ass1'}".replace('\'', '"'));

        AssertionChecker checker = new AssertionChecker(Collections.singletonList(a1desc));

        checker.check(messageProcessorMock, null);
    }

    @Test(expected = AssertionFailureException.class)
    public void Should_throwWhenCannotReadValueFromEnvironment()
            throws Exception {
        IObject a1desc = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{'type': 'atype1', 'name': 'Ass1'}".replace('\'', '"'));

        Mockito.when(environmentMock.getValue(Matchers.any())).thenThrow(ReadValueException.class);

        AssertionChecker checker = new AssertionChecker(Collections.singletonList(a1desc));

        checker.check(messageProcessorMock, null);
    }

    @Test(expected = AssertionFailureException.class)
    public void Should_throwWhenAssertionFails()
            throws Exception {
        IObject a1desc = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{'type': 'atype1', 'name': 'Ass1'}".replace('\'', '"'));

        Mockito.doThrow(AssertionFailureException.class).when(assertion1Mock).check(Matchers.same(a1desc), Matchers.any());

        AssertionChecker checker = new AssertionChecker(Collections.singletonList(a1desc));

        checker.check(messageProcessorMock, null);
    }

    @Test(expected = AssertionFailureException.class)
    public void Should_throwWhenExceptionOccurs()
            throws Exception {
        IObject a1desc = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{'type': 'atype1', 'name': 'Ass1'}".replace('\'', '"'));

        Mockito.doThrow(AssertionFailureException.class).when(assertion1Mock).check(Matchers.same(a1desc), Matchers.any());

        AssertionChecker checker = new AssertionChecker(Collections.singletonList(a1desc));

        checker.check(messageProcessorMock, new Exception());
    }

    @Test
    public void Should_createWrapperDescription()
            throws Exception {
        IObject a1desc = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{'type': 'atype1', 'name': 'Ass1', 'value': 'message/x'}".replace('\'', '"'));

        Mockito.doThrow(AssertionFailureException.class).when(assertion1Mock).check(Matchers.same(a1desc), Matchers.any());

        AssertionChecker checker = new AssertionChecker(Collections.singletonList(a1desc));

        IObject desc = checker.getSuccessfulReceiverArguments();
        IObject wrapper = (IObject) desc.getValue(new FieldName("wrapper"));
        assertNotNull(wrapper);
        List<IObject> transformationRules = (List<IObject>) wrapper.getValue(new FieldName("in_Ass1"));
        assertNotNull(transformationRules);
        assertEquals(transformationRules.size(), 1);
    }
}
