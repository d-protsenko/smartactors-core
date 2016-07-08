package info.smart_tools.smartactors.core.wrapper_generator;

import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.ds_object.FieldName;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject_wrapper.IObjectWrapper;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iwrapper_generator.IWrapperGenerator;
import info.smart_tools.smartactors.core.iwrapper_generator.exception.WrapperGeneratorException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WrapperGenerator}
 */
public class WrapperGeneratorTest {

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
                Keys.getOrAdd(IFieldName.class.getCanonicalName()),
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
                Keys.getOrAdd(IObject.class.getCanonicalName()),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            try {
                                return new DSObject();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );
        IOC.register(
                Keys.getOrAdd(IResolveDependencyStrategy.class.getCanonicalName()),
                new ResolveByNameIocStrategy(
                        (a) -> a[1]
                )
        );

        fillBinding();
    }

    private void fillBinding()
            throws Exception {
        IObject env = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()), "environments");

        IObject bindingForIIncorrectWrapperWithoutReadValueException = new DSObject("{\n" +
                "    \"in_getValue\": \"message/Value\"\n" +
                "  }");
        IObject bindingForIIncorrectWrapperWithoutChangeValueException = new DSObject("{\n" +
                "    \"out_getValue\": \"response/Value\"\n" +
                "  }");
        IObject bindingForIInnerWrapper = new DSObject("{\n" +
                "  \"in_getDoubleValue\": \"message/DoubleValue\",\n" +
                "  \"out_setDoubleValue\": \"response/DoubleValue\"\n" +
                "}");
        IObject bindingForIWrapper = new DSObject("{\n" +
                "    \"in_getIntValue\": \"message/IntValue\",\n" +
                "    \"out_setIntValue\": \"response/IntValue\",\n" +
                "    \"in_getTestClassValue\": \"message/TestClassValue\",\n" +
                "    \"out_setTestClassValue\": \"response/TestClassValue\",\n" +
                "    \"out_getListOfTestClasses\": \"response/ListOfTestClasses\",\n" +
                "    \"out_wrappedIObject\": \"response/WrappedIObject\",\n" +
                "    \"in_wrappedIObject\": [{\n" +
                "      \"name\": \"ConvertToWrapper\",\n" +
                "      \"args\": [\"environment\", \"const/info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper\"]\n" +
                "    }],\n" +
                "    \"in_getListOfTestClasses\": [{\n" +
                "      \"args\": [\"message/TestClassValue\"]\n" +
                "    }, {\n" +
                "      \"name\": \"AddToList\",\n" +
                "      \"args\": [\"context/ListOfTestClasses\", \"local/value\"]\n" +
                "    }, {\n" +
                "      \"args\": [\"message/OtherTestClassValue\"]\n" +
                "    }, {\n" +
                "      \"name\": \"AddToList\",\n" +
                "      \"args\": [\"context/ListOfTestClasses\", \"local/value\"]\n" +
                "    }, {\n" +
                "      \"args\": [\"context/ListOfTestClasses\"]\n" +
                "    }],\n" +
                "    \"out_transform\": [\n" +
                "      [{\n" +
                "        \"name\": \"ConvertToString\",\n" +
                "        \"args\": [\"local/value\"]\n" +
                "      }, {\n" +
                "        \"name\": \"JoinStrings\",\n" +
                "        \"args\": [\"local/value\", \"const/CONST\"]\n" +
                "      }, {\n" +
                "        \"name\": \"target\",\n" +
                "        \"args\": [\"response/ModifiedString1\", \"local/value\"]\n" +
                "      }],\n" +
                "      [{\n" +
                "        \"name\": \"ConvertToString\",\n" +
                "        \"args\": [\"local/value\"]\n" +
                "      }, {\n" +
                "        \"name\": \"target\",\n" +
                "        \"args\": [\"response/ModifiedString2\", \"local/value\"]\n" +
                "      }]\n" +
                "    ]\n" +
                "  }");
        IObject maps = new DSObject();
        maps.setValue(
                new FieldName(IIncorrectWrapperWithoutReadValueException.class.getCanonicalName()),
                bindingForIIncorrectWrapperWithoutReadValueException
        );
        maps.setValue(
                new FieldName(IIncorrectWrapperWithoutChangeValueException.class.getCanonicalName()),
                bindingForIIncorrectWrapperWithoutChangeValueException
        );
        maps.setValue(
                new FieldName(IWrapper.class.getCanonicalName()),
                bindingForIWrapper
        );
        maps.setValue(
                new FieldName(IInnerWrapper.class.getCanonicalName()),
                bindingForIInnerWrapper
        );

        env.setValue(new FieldName("wrappers"), maps);
    }

    @Test
    public void checkCreationAndUsageWrapperByInterface()
            throws Exception {
        IResolveDependencyStrategy toWrapperConverter = mock(IResolveDependencyStrategy.class);
        IOC.register(Keys.getOrAdd("ConvertToWrapper"), toWrapperConverter);
        IOC.resolve(
                Keys.getOrAdd(IResolveDependencyStrategy.class.getCanonicalName()), "ConvertToWrapper", toWrapperConverter
        );

        IResolveDependencyStrategy addToList = mock(IResolveDependencyStrategy.class);
        IOC.register(Keys.getOrAdd("AddToList"), addToList);
        IOC.resolve(
                Keys.getOrAdd(IResolveDependencyStrategy.class.getCanonicalName()), "AddToList", addToList
        );


        IResolveDependencyStrategy convertToString = mock(IResolveDependencyStrategy.class);
        IOC.register(Keys.getOrAdd("ConvertToString"), convertToString);
        IOC.resolve(
                Keys.getOrAdd(IResolveDependencyStrategy.class.getCanonicalName()), "ConvertToString", convertToString
        );

        IResolveDependencyStrategy joinStrings = mock(IResolveDependencyStrategy.class);
        IOC.register(Keys.getOrAdd("JoinStrings"), joinStrings);
        IOC.resolve(
                Keys.getOrAdd(IResolveDependencyStrategy.class.getCanonicalName()), "JoinStrings", joinStrings
        );

        IWrapperGenerator wg = new WrapperGenerator(null);
        IWrapper inst = wg.generate(IWrapper.class);
        assertNotNull(inst);

        IObject env = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()), "environments");
        IInnerWrapper innerWrapper = mock(IInnerWrapper.class);
        when(toWrapperConverter.resolve(env, "info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper")).thenReturn(innerWrapper);

        IObject message = mock(IObject.class);
        when(message.getValue(new FieldName("IntValue"))).thenReturn(1);
        when(message.getValue(new FieldName("StringValue"))).thenReturn("abc");
        when(message.getValue(new FieldName("IObject"))).thenReturn(new DSObject());
        TestClass testClass = mock(TestClass.class);
        when(message.getValue(new FieldName("TestClassValue"))).thenReturn(testClass);
        TestClass otherTestClass = mock(TestClass.class);
        when(message.getValue(new FieldName("OtherTestClassValue"))).thenReturn(otherTestClass);

        IObject context = mock(IObject.class);
        List<TestClass> listOfTestClasses = new ArrayList<>();
        when(context.getValue(new FieldName("ListOfTestClasses"))).thenReturn(listOfTestClasses);
        when(addToList.resolve(listOfTestClasses, testClass)).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ((List<TestClass>) args[0]).add((TestClass) args[1]);
                return null;
            }
        });
        when(addToList.resolve(listOfTestClasses, otherTestClass)).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ((List<TestClass>) args[0]).add((TestClass) args[1]);
                return null;
            }
        });

        IObject response = mock(IObject.class);

        env.setValue(new FieldName("message"), message);
        env.setValue(new FieldName("context"), context);
        env.setValue(new FieldName("response"), response);

        ((IObjectWrapper)inst).init(env);

        // Check 'in' methods of wrapper
        int intResult = inst.getIntValue();
        assertEquals(intResult, 1);
        TestClass testClassResult = inst.getTestClassValue();
        assertSame(testClassResult, testClass);
        IInnerWrapper innerWrapperResult = inst.wrappedIObject();
        assertSame(innerWrapperResult, innerWrapper);
        List<TestClass> testClasses = inst.getListOfTestClasses();
        assertEquals(testClasses.size(), 2);
        assertSame(testClasses.get(0), testClass);
        assertSame(testClasses.get(1), otherTestClass);

        // Check 'out' methods of wrapper


        // Check methods of IObjectWrapper
        IObject resultMessage = ((IObjectWrapper) inst).getEnvironmentIObject(new FieldName("message"));
        IObject resultContext = ((IObjectWrapper) inst).getEnvironmentIObject(new FieldName("context"));
        IObject resultResponse = ((IObjectWrapper) inst).getEnvironmentIObject(new FieldName("response"));
//
//        assertEquals(intResult, 1);
//        assertEquals(stringResult, "abc");
//        assertEquals(testClassResult, testClass);
//        assertEquals((int) integerList.get(0), 1);
//        assertEquals(stringList.get(0), "abc");
//        assertEquals(testClasses.get(0).getF(), 1.5f, 0);
//        assertEquals(boolResult, true);
//        assertEquals(innerWrapperResult, innerWrapper);
//        assertEquals(iObjectResult, iObject);
//
//        assertNotNull(resultOfChainStrategy);
//        assertTrue(IInnerWrapper.class.isAssignableFrom(resultOfChainStrategy.getClass()));
//
//        assertSame(resultMessage, message);
//        assertSame(resultContext, context);
//        assertSame(resultResponse, response);
//
//        inst.setIntValue(2);
//        inst.setStringValue("cba");
//        TestClass testClassForSetter = new TestClass();
//        inst.setTestClassValue(testClassForSetter);
//        List<Integer> integerListForSetter = new ArrayList<Integer>(){{add(2);}};
//        inst.setListOfInt(integerListForSetter);
//        List<String> stringListForSetter = new ArrayList<String>(){{add("cba");}};
//        inst.setListOfString(stringListForSetter);
//        List<TestClass> testClassListForSetter = new ArrayList<TestClass>(){{add(testClassForSetter);}};
//        inst.setListOfTestClasses(testClassListForSetter);
//        inst.setBoolValue(false);
//        IInnerWrapper innerWrapperForSetter = mock(IInnerWrapper.class);
//        inst.setWrappedIObject(innerWrapperForSetter);
//        IObject iObjectForSetter = mock(IObject.class);
//        inst.setIObject(iObjectForSetter);
//        inst.setStringIInnerMap(new HashMap<String, IInnerWrapper>(){{put("cba", innerWrapperForSetter);}});
//
//        verify(response, times(1)).setValue(new FieldName("IntValue"), 2);
//        verify(response, times(1)).setValue(new FieldName("StringValue"), "cba");
//        verify(response, times(1)).setValue(new FieldName("TestClassValue"), testClassForSetter);
//        verify(response, times(1)).setValue(new FieldName("ListOfInt"), integerListForSetter);
//        verify(response, times(1)).setValue(new FieldName("ListOfString"), stringListForSetter);
//        verify(response, times(1)).setValue(new FieldName("ListOfTestClasses"), testClassListForSetter);
//        verify(response, times(1)).setValue(new FieldName("BoolValue"), false);
//        verify(response, times(1)).setValue(new FieldName("WrappedIObject"), innerWrapperForSetter);
//        verify(response, times(1)).setValue(new FieldName("IObject"), iObjectForSetter);
//        verify(subContext, times(1)).setValue(new FieldName("StringIInnerMap"), new HashMap<String, IInnerWrapper>(){{put("cba", innerWrapperForSetter);}});
    }

    @Test (expected = WrapperGeneratorException.class)
    public void checkOnIncorrectInterfaceWithoutReadValueException()
            throws Exception {
        IWrapperGenerator wg = new WrapperGenerator(null);
        wg.generate(IIncorrectWrapperWithoutReadValueException.class);
    }

    @Test (expected = WrapperGeneratorException.class)
    public void checkOnIncorrectInterfaceWithoutChangeValueException()
            throws Exception {
        IWrapperGenerator wg = new WrapperGenerator(null);
        wg.generate(IIncorrectWrapperWithoutChangeValueException.class);
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnTargetInterfaceNull()
            throws Exception {
        IWrapperGenerator wg = new WrapperGenerator(null);
        wg.generate(null);
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnNotInterface()
            throws Exception {
        IWrapperGenerator wg = new WrapperGenerator(null);
        wg.generate(TestClass.class);
        fail();
    }
}

