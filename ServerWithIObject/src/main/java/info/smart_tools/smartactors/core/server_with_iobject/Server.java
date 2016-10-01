package info.smart_tools.smartactors.core.server_with_iobject;

import info.smart_tools.smartactors.iobject_extension.configuration_object.ConfigurationObject;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.core.iserver.IServer;
import info.smart_tools.smartactors.core.iserver.exception.ServerExecutionException;
import info.smart_tools.smartactors.core.iserver.exception.ServerInitializeException;
import info.smart_tools.smartactors.message_processing_interfaces.iwrapper_generator.IWrapperGenerator;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.ioc.string_ioc_key.Key;
import info.smart_tools.smartactors.iobject_extension.wds_object.WDSObject;
import info.smart_tools.smartactors.message_processing.wrapper_generator.WrapperGenerator;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link IServer} with
 */
public class Server implements IServer {

    private IObject ds_config;
    private IObject co_config;

    @Override
    public void initialize() throws ServerInitializeException {
        try {
            scopeInit();
            registerKeysStorageStrategyAndFieldNameStrategy();
            registerWrapperGenerator();
            initObjects();
        } catch (Throwable e) {
            throw new ServerInitializeException("Could not initialize server.");
        }
    }

    @Override
    public void start()
            throws ServerExecutionException {
        try {
            /** Get wrapper generator by IOC.resolve */
            IWrapperGenerator wg = IOC.resolve(Keys.getOrAdd(IWrapperGenerator.class.getCanonicalName()));

            /** Get message, context, response */
            IObject message = getMessage();
            IObject context = getContext();
            IObject response = getResponse();
            IObject environment = new DSObject();
            environment.setValue(new FieldName("message"), message);
            environment.setValue(new FieldName("context"), context);
            environment.setValue(new FieldName("response"), response);

            /** Generate wrapper class by given interface and create instance of generated class */
            IWrapper wrapper = wg.generate(IWrapper.class);

            /** Check registration of IWrapper instance creation strategy to IOC */
            IWrapper newInstanceOfWrapper = IOC.resolve(Keys.getOrAdd(IWrapper.class.getCanonicalName() + "wrapper"));

            WDSObject wds = new WDSObject(((IObject) this.co_config.getValue(new FieldName("wrapper"))));

            /** Initialize wrapper */
            wds.init(environment);
            ((IObjectWrapper) wrapper).init(wds);

            /** Wrapper usage: get values */
            Integer i = wrapper.getIntValue();
            String s = wrapper.getStringValue();
            Boolean b = wrapper.getBoolValue();
            IObject io = wrapper.getIObject();
            List<Integer> intList = wrapper.getListOfInt();
            List<String> stringList = wrapper.getListOfString();

            /** Wrapper usage: set values */
            wrapper.setIntValue(2);
            wrapper.setBoolValue(false);
            wrapper.setStringValue("new text");
            wrapper.setIObject(new DSObject());
            wrapper.setListOfString(new ArrayList<String>() {{ add("new string"); }});
            wrapper.setListOfInt(new ArrayList<Integer>() {{ add(2); }});

        } catch (Throwable e) {
            throw new ServerExecutionException(e);
        }
    }

    private void scopeInit()
            throws Exception {
        ScopeProvider.subscribeOnCreationNewScope(
                scope -> {
                    try {
                        scope.setValue(IOC.getIocKey(), new StrategyContainer());
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }
        );


        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope mainScope = ScopeProvider.getScope(keyOfMainScope);
        ScopeProvider.setCurrentScope(mainScope);
    }

    private void registerKeysStorageStrategyAndFieldNameStrategy()
            throws Exception {
        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy(
                        (arg) -> {
                            try {
                                return new Key((String) arg[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
        );
        IOC.register(
                Keys.getOrAdd(FieldName.class.getCanonicalName()),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            try {
                                return new FieldName((String) a[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );
    }

    private void registerWrapperGenerator()
            throws Exception {
        IWrapperGenerator wg = new WrapperGenerator(null);
        IOC.register(Keys.getOrAdd(IWrapperGenerator.class.getCanonicalName()), new SingletonStrategy(wg));
    }

    private IObject getMessage()
            throws Exception {
        IObject obj = new DSObject();
        obj.setValue(new FieldName("IntValue"), 1);
        obj.setValue(new FieldName("StringValue"), "some text");
        obj.setValue(
                new FieldName("ListOfInt"),
                new ArrayList<Integer>() {{ add(1); add(2); }}
        );
        obj.setValue(
                new FieldName("ListOfString"),
                new ArrayList<String>() {{ add("some text"); add("another text"); }}
        );

        return obj;
    }

    private IObject getContext()
            throws Exception {
        IObject obj = new DSObject();
        obj.setValue(new FieldName("BoolValue"), true);
        obj.setValue(new FieldName("IObject"), new DSObject());

        return obj;
    }

    private IObject getResponse()
            throws Exception {
        IObject obj = new DSObject();

        return obj;
    }

    private void initObjects()
            throws Exception {
        IOC.register(
                IOC.resolve(
                        IOC.getKeyForKeyStorage(), "configuration object"
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
        IOC.register(
                IOC.resolve(
                        IOC.getKeyForKeyStorage(), "configuration object default strategy"
                ),
                new ApplyFunctionToArgumentsStrategy(
                        (a) -> {
                            try {
                                return a[0];
                            } catch (Throwable e) {
                                throw new RuntimeException(
                                        "Error in configuration 'default' rule.", e
                                );
                            }
                        }
                )
        );
        IOC.register(
                IOC.resolve(
                        IOC.getKeyForKeyStorage(), "configuration object in_ strategy"
                ),
                new ApplyFunctionToArgumentsStrategy(
                        (a) -> {
                            try {
                                Object obj = a[0];
                                if (obj instanceof String) {
                                    IObject innerObject = new ConfigurationObject();
                                    innerObject.setValue(new FieldName("name"), "wds_getter_strategy");
                                    innerObject.setValue(new FieldName("args"), new ArrayList<String>() {{ add((String) obj); }} );

                                    return new ArrayList<IObject>() {{ add(innerObject); }};
                                }
                                return obj;
                            } catch (Throwable e) {
                                throw new RuntimeException(
                                        "Error in configuration 'wrapper' rule.", e
                                );
                            }
                        }
                )
        );
        IOC.register(
                IOC.resolve(
                        IOC.getKeyForKeyStorage(), "configuration object out_ strategy"
                ),
                new ApplyFunctionToArgumentsStrategy(
                        (a) -> {
                            try {
                                Object obj = a[0];
                                if (obj instanceof String) {
                                    IObject innerObject = new ConfigurationObject();
                                    innerObject.setValue(new FieldName("name"), "wds_target_strategy");
                                    innerObject.setValue(new FieldName("args"), new ArrayList<String>() {{ add("local/value"); add((String) obj); }} );

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
                )
        );
        IOC.register(
                IOC.resolve(
                        IOC.getKeyForKeyStorage(), "resolve key for configuration object"
                ),
                new ApplyFunctionToArgumentsStrategy(
                        (a) -> {
                            try {
                                Map<String, String> keys = new HashMap<String, String>() {{
                                    put("in_", "configuration object in_ strategy");
                                    put("out_", "configuration object out_ strategy");
                                }};
                                char[] symbols = a[1].toString().toCharArray();
                                String resolvedKey = "configuration object default strategy";
                                StringBuilder key = new StringBuilder();
                                for (char c : symbols) {
                                    key.append(c);
                                    if (null != keys.get(key.toString())) {
                                        resolvedKey = keys.get(key.toString());
                                        break;
                                    }
                                }
                                return IOC.resolve(
                                        IOC.resolve(IOC.getKeyForKeyStorage(), resolvedKey),
                                        a[0]
                                );
                            } catch (Throwable e) {
                                throw new RuntimeException(
                                        "Configuration object key resolution failed."
                                );
                            }
                        }
                )
        );
        this.co_config = new DSObject("{\n" +
                "  \"wrapper\": {\n" +
                "    \"in_getIntValue\": [{\n" +
                "      \"name\": \"wds_getter_strategy\",\n" +
                "      \"args\": [\"message/IntValue\"]\n" +
                "    }],\n" +
                "    \"out_setIntValue\": [\n" +
                "      [{\n" +
                "        \"name\": \"wds_target_strategy\",\n" +
                "        \"args\": [\"local/value\", \"response/IntValue\"]\n" +
                "      }]\n" +
                "    ],\n" +
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
                "        \"name\": \"wds_target_strategy\",\n" +
                "        \"args\": [\"local/value\", \"response/IObject\"]\n" +
                "      }]\n" +
                "    ]\n" +
                "  }\n" +
                "}");
    }
}