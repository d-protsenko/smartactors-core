package info.smart_tools.smartactors.iobject_extension.configuration_object;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.istrategy_registration.IStrategyRegistration;
import info.smart_tools.smartactors.base.interfaces.istrategy.IStrategy;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.DeleteValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.ioc.string_ioc_key.Key;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for {@link ConfigurationObject}
 */
public class ConfigurationObjectTest {

    private String configString;

    @Before
    public void init()
            throws Exception {
        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope scope = ScopeProvider.getScope(keyOfMainScope);
        scope.setValue(IOC.getIocKey(), new StrategyContainer());
        ScopeProvider.setCurrentScope(scope);

        IOC.register(
                IOC.getKeyForKeyByNameStrategy(),
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
                IOC.resolve(
                        IOC.getKeyForKeyByNameStrategy(), "configuration object"
                ),
                new ApplyFunctionToArgumentsStrategy(
                        (a) -> {
                            try {
                                return new ConfigurationObject((String) a[0]);
                            } catch (Throwable e) {
                                throw new RuntimeException(
                                        "Could not create new instance of Configuration Object."
                                );
                            }
                        }
                )
        );
        IStrategy defaultStrategy = new ApplyFunctionToArgumentsStrategy(
                (a) -> {
                    try {
                        return a[1];
                    } catch (Throwable e) {
                        throw new RuntimeException(
                                "Error in configuration 'default' rule.", e
                        );
                    }
                }
        );
        IStrategy inStrategy = new ApplyFunctionToArgumentsStrategy(
                (a) -> {
                    try {
                        Object obj = a[1];
                        if (obj instanceof String) {
                            IObject innerObject = new ConfigurationObject();
                            innerObject.setValue(new FieldName("name"), "wds_getter_strategy");
                            innerObject.setValue(new FieldName("args"), new ArrayList<String>() {{ add((String) obj); }});

                            return new ArrayList<IObject>() {{ add(innerObject); }};
                        }
                        return obj;
                    } catch (Throwable e) {
                        throw new RuntimeException(
                                "Error in configuration 'wrapper' rule.", e
                        );
                    }
                }
        );
        IStrategy outStrategy = new ApplyFunctionToArgumentsStrategy(
                (a) -> {
                    try {
                        Object obj = a[1];
                        if (obj instanceof String) {
                            IObject innerObject = new ConfigurationObject();
                            innerObject.setValue(new FieldName("name"), "wds_target_strategy");
                            innerObject.setValue(new FieldName("args"), new ArrayList<String>() {{ add("local/value"); add((String) obj); }});

                            return new ArrayList<List<IObject>>() {{
                                add(new ArrayList<IObject>() {{  add(innerObject); }});
                            }};
                        }
                        if (obj instanceof List) {
                            for (Object o : (List) obj) {
                                if (o instanceof List) {
                                    for (Object innerObject : (List) o) {
                                        if (((IObject) innerObject).getValue(new FieldName("name")).equals("target")) {
                                            ((IObject) innerObject).setValue(new FieldName("name"), "wds_target_strategy");
                                            ((IObject) innerObject).setValue(new FieldName("args"), new ArrayList<String>() {{
                                                        add("local/value");
                                                        add((String) ((List) ((IObject) innerObject)
                                                                .getValue(new FieldName("args"))).get(0));
                                                    }}
                                            );
                                        }
                                    }
                                }
                            }
                        }
                        return obj;
                    } catch (Throwable e) {
                        throw new RuntimeException("Error in configuration 'wrapper' rule.", e);
                    }
                }
        );
        IStrategy strategy = new CObjectStrategy();
        ((IStrategyRegistration) strategy).register("in_", inStrategy);
        ((IStrategyRegistration) strategy).register("out_", outStrategy);
        ((IStrategyRegistration) strategy).register("default", defaultStrategy);
        IOC.register(
                IOC.resolve(
                        IOC.getKeyForKeyByNameStrategy(), "resolve key for configuration object"
                ),
                strategy
        );
        this.configString = "{\n" +
                "  \"wrapper\": {\n" +
                "    \"in_getIntValue\": \"message/IntValue\",\n" +
                "    \"out_setIntValue\": \"response/IntValue\",\n" +
                "    \"in_getStringValue\": [{\n" +
                "      \"name\": \"wds_getter_strategy\",\n" +
                "      \"args\": [\"message/StringValue\"]\n" +
                "    }],\n" +
                "    \"out_setStringValue\": [\n" +
                "      [{\n" +
                "        \"name\": \"wds_target_strategy\",\n" +
                "        \"args\": [\"local/value\", \"response/StringValue\"]\n" +
                "      }]\n" +
                "    ],\n" +
                "    \"in_getListOfInt\": [{\n" +
                "      \"name\": \"wds_getter_strategy\",\n" +
                "      \"args\": [\"message/ListOfInt\"]\n" +
                "    }],\n" +
                "    \"out_setListOfInt\": [\n" +
                "      [{\n" +
                "        \"name\": \"wds_target_strategy\",\n" +
                "        \"args\": [\"local/value\", \"response/ListOfInt\"]\n" +
                "      }]\n" +
                "    ],\n" +
                "    \"in_getListOfString\": [{\n" +
                "      \"name\": \"wds_getter_strategy\",\n" +
                "      \"args\": [\"message/ListOfString\"]\n" +
                "    }],\n" +
                "    \"out_setListOfString\": [\n" +
                "      [{\n" +
                "        \"name\": \"wds_target_strategy\",\n" +
                "        \"args\": [\"local/value\", \"response/ListOfString\"]\n" +
                "      }]\n" +
                "    ],\n" +
                "    \"in_getBoolValue\": [{\n" +
                "      \"name\": \"wds_getter_strategy\",\n" +
                "      \"args\": [\"context/BoolValue\"]\n" +
                "    }],\n" +
                "    \"out_setBoolValue\": [\n" +
                "      [{\n" +
                "        \"name\": \"wds_target_strategy\",\n" +
                "        \"args\": [\"local/value\", \"response/BoolValue\"]\n" +
                "      }]\n" +
                "    ],\n" +
                "    \"in_getIObject\": [{\n" +
                "      \"name\": \"wds_getter_strategy\",\n" +
                "      \"args\": [\"context/IObject\"]\n" +
                "    }],\n" +
                "    \"out_setIObject\": [\n" +
                "      [{\n" +
                "        \"name\": \"target\",\n" +
                "        \"args\": [\"response/IObject\"]\n" +
                "      }]\n" +
                "    ]\n" +
                "  }\n" +
                "}";
    }

    @Test
    public void checkCreationAndResolutionSomeFields()
            throws Exception {
        ConfigurationObject co = new ConfigurationObject(this.configString);

        List<IObject> in_getIntValue = (List<IObject>) ((IObject)co.getValue(new FieldName("wrapper"))).getValue(new FieldName("in_getIntValue"));
        assertEquals(in_getIntValue.get(0).getValue(new FieldName("name")), "wds_getter_strategy");
        assertEquals(((List)in_getIntValue.get(0).getValue(new FieldName("args"))).get(0), "message/IntValue");

        List<List<IObject>> out_setIntValue = (List<List<IObject>>) ((IObject)co.getValue(new FieldName("wrapper"))).getValue(new FieldName("out_setIntValue"));
        assertEquals(out_setIntValue.get(0).get(0).getValue(new FieldName("name")), "wds_target_strategy");
        assertEquals(((List)out_setIntValue.get(0).get(0).getValue(new FieldName("args"))).get(0), "local/value");
        assertEquals(((List)out_setIntValue.get(0).get(0).getValue(new FieldName("args"))).get(1), "response/IntValue");

        List<List<IObject>> out_setIObject = (List<List<IObject>>) ((IObject)co.getValue(new FieldName("wrapper"))).getValue(new FieldName("out_setIObject"));
        assertEquals(out_setIObject.get(0).get(0).getValue(new FieldName("name")), "wds_target_strategy");
        assertEquals(((List)out_setIObject.get(0).get(0).getValue(new FieldName("args"))).get(0), "local/value");
        assertEquals(((List)out_setIObject.get(0).get(0).getValue(new FieldName("args"))).get(1), "response/IObject");

        List<IObject> in_getStringValue = (List<IObject>) ((IObject)co.getValue(new FieldName("wrapper"))).getValue(new FieldName("in_getStringValue"));
        assertEquals(in_getStringValue.get(0).getValue(new FieldName("name")), "wds_getter_strategy");
        assertEquals(((List)in_getStringValue.get(0).getValue(new FieldName("args"))).get(0), "message/StringValue");
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkCreationExceptionOnNullArg()
            throws Exception {
        ConfigurationObject co = new ConfigurationObject("");
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkExceptionOnNullArgInGetValueMethod()
            throws Exception {
        ConfigurationObject co = new ConfigurationObject(this.configString);
        co.getValue(null);
        fail();
    }

    @Test (expected = DeleteValueException.class)
    public void checkExceptionOnUseDeleteFieldMethod()
            throws Exception {
        ConfigurationObject co = new ConfigurationObject(this.configString);
        co.deleteField(null);
        fail();
    }

    @Test
    public void checkNullOnTryToGetIterator()
            throws Exception {
        ConfigurationObject co = new ConfigurationObject(this.configString);
        assertNull(co.iterator());
    }

    @Test (expected = ReadValueException.class)
    public void checkReadValueExceptionOnUseGetValueMethod() throws Exception {
        Object scopeId = ScopeProvider.createScope(null);
        IScope oldScope = ScopeProvider.getCurrentScope();
        ScopeProvider.setCurrentScope(ScopeProvider.getScope(scopeId));

        ConfigurationObject co = new ConfigurationObject(this.configString);
        co.getValue(new FieldName("in_getIntValue"));
        ScopeProvider.setCurrentScope(oldScope);
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnOnNullFieldNameInSetValue()
            throws Exception {
        IObject object = new ConfigurationObject();
        object.setValue(null, new Object());
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnCreationWithNullMap()
            throws Exception {
        Map entries = null;
        IObject object = new ConfigurationObject(entries);
        fail();
    }

    @Test
    public void Should_serializeAsCanonizedObject()
            throws Exception {
        String source = "{'in_getX':'m/x'}".replace('\'','"');

        IObject cObj = new ConfigurationObject(source);

        String serialized = cObj.serialize();

        assertTrue(
                serialized.equals("{\"in_getX\":[{\"args\":[\"m/x\"],\"name\":\"wds_getter_strategy\"}]}") ||
                serialized.equals("{\"in_getX\":[{\"name\":\"wds_getter_strategy\",\"args\":[\"m/x\"]}]}"));
    }
}
