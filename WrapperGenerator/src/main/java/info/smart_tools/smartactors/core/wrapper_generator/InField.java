package info.smart_tools.smartactors.core.wrapper_generator;

import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link IField} only for {@code in} method
 * @param <T> return type for {@code in} method
 */
public class InField implements IField {

    private static final String FIELD_ARGS = "args";
    private static final String FIELD_NAME = "name";
    private static final String LOCAL_KEYWORD = "local/value";
    private static final String CONST_KEYWORD = "const/";
    private static final String ENVIRONMENT_KEYWORD = "environment";
    private static final String SPLITTER = "\\/";
    private static final String MAPS_KEYWORD = "environments";
    private static final String SUB_MAPS_KEYWORD = "wrappers";

    private List<Map<String, Object>> rules;
    private HashMap<String, IResolveDependencyStrategy> strategies;

    /**
     * Constructor.
     * Create instance of {@link InField}
     * @param bindingPath the path to binding for current instance of {@link InField}
     * @throws InvalidArgumentException if incoming arguments are incorrect
     */
    public InField(final String bindingPath)
            throws InvalidArgumentException {
        try {
            this.strategies = new HashMap<>();
            Object obj = getValueFromNestedIObject(
                    IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()), MAPS_KEYWORD),
                    SUB_MAPS_KEYWORD + "/" + bindingPath
            );
            if (obj instanceof ArrayList) {
                this.rules = (ArrayList) obj;
                for (Map<String, Object> rule : (ArrayList<Map<String, Object>>) obj) {
                    String name = (String) rule.get(FIELD_NAME);
                    if (name != null && !name.isEmpty()) {
                        strategies.put(
                                name,
                                IOC.resolve(Keys.getOrAdd(IResolveDependencyStrategy.class.getCanonicalName()), name)
                        );
                    }
                }
            }
            if (obj instanceof String) {
                this.rules = new ArrayList<>();
                this.rules.add(
                        new HashMap<String, Object>() {{
                            put("args", new ArrayList<String>() {{ add((String) obj); }});
                        }}
                );
            }
        } catch (Throwable e) {
            throw new InvalidArgumentException("" , e);
        }
    }

    @Override
    public <T> T in(final IObject env)
            throws ReadValueException, InvalidArgumentException, ClassCastException {
        if (null == env) {
            throw new InvalidArgumentException("Environment should not be null.");
        }
        Object value = null;
        try {
            for (Map<String, Object> rule : this.rules) {
                List<String> args = (ArrayList<String>) rule.get(FIELD_ARGS);
                String name = (String) rule.get(FIELD_NAME);
                Object[] resolvedArgs = new Object[args.size()];
                for (String arg : args) {
                    if (arg.equals(LOCAL_KEYWORD)) {
                        resolvedArgs[args.indexOf(arg)] = value;
                        continue;
                    }
                    if (arg.contains(CONST_KEYWORD)) {
                        resolvedArgs[args.indexOf(arg)] = arg.split(SPLITTER)[1];
                        continue;
                    }
                    if (arg.equals(ENVIRONMENT_KEYWORD)) {
                        resolvedArgs[args.indexOf(arg)] = env;
                        continue;
                    }
                    resolvedArgs[args.indexOf(arg)] = getValueFromNestedIObject(env, arg);
                }
                value =
                        null != name && !name.isEmpty() ?
                        IOC.resolve(Keys.getOrAdd(name), resolvedArgs) :
                        resolvedArgs[0];
            }
        } catch (Exception e) {
            throw new ReadValueException("Could not apply rules.", e);
        }
        if (null == value) {
            return null;
        }

        return (T) value;
    }

    @Override
    public <T> void out(final IObject env, final T in)
            throws ChangeValueException, InvalidArgumentException {
        throw new InvalidArgumentException("Method not implemented.");
    }

    private Object getValueFromNestedIObject(final IObject source, final String location)
            throws ReadValueException {
        try {
            String[] separated = location.split(SPLITTER);
            return getNestedIObject(source, separated).getValue(
                    IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), separated[separated.length - 1])
            );
        } catch (Throwable e) {
            throw new ReadValueException("Could not read value from IObject", e);
        }
    }

    private IObject getNestedIObject(final IObject source, final String[] separatedPath) throws Exception {
        try {
            IObject nestedObject = (IObject) source.getValue(
                    IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), separatedPath[0])
            );
            for (int i = 1; i < separatedPath.length - 1; ++i) {
                nestedObject = (IObject) nestedObject.getValue(
                        IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), separatedPath[i])
                );
            }

            return nestedObject;
        } catch (Throwable e) {
            throw new ReadValueException("Could not read nested IObject.", e);
        }
    }
}
