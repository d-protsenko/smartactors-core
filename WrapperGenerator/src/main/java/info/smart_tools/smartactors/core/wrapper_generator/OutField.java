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
 * Implementation of {@link IField} only for {@code out} method
 * @param <T> return type for {@code in} method
 */
public class OutField<T> implements IField<T> {

    private static final String FIELD_ARGS = "args";
    private static final String FIELD_NAME = "name";
    private static final String LOCAL_KEYWORD = "local";
    private static final String VALUE_KEYWORD = "value";
    private static final String SPLITTER = "\\/";
    private static final String MAPS_KEYWORD = "environments";
    private static final String SUB_MAPS_KEYWORD = "wrappers";

    private List<List<Map<String, Object>>> rules;
    private HashMap<String, IResolveDependencyStrategy> strategies;

    /**
     * Constructor.
     * Create instance of {@link OutField}
     * @param bindingPath the path to binding for current instance of {@link OutField}
     * @throws InvalidArgumentException if incoming arguments are incorrect
     */
    public OutField(final String bindingPath)
            throws InvalidArgumentException {
        try {
            this.strategies = new HashMap<>();
            Object obj = getValueFromNestedIObject(
                    IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()), MAPS_KEYWORD),
                    SUB_MAPS_KEYWORD + "/" + bindingPath
            );
            if (obj instanceof ArrayList) {
                this.rules = (ArrayList) obj;
                for (List<Map<String, Object>> rule: (ArrayList<ArrayList<Map<String, Object>>>) obj) {
                    for (Map<String, Object> subRule : (ArrayList<Map<String, Object>>) rule) {
                        String name = (String) subRule.get(FIELD_NAME);
                        if (name != null && !name.isEmpty()) {
                            strategies.put(
                                    name,
                                    IOC.resolve(Keys.getOrAdd(IResolveDependencyStrategy.class.getCanonicalName()), name)
                            );
                        }
                    }
                }
            }
            if (obj instanceof String) {
                this.rules = new ArrayList<>();
                this.rules.add(
                        new ArrayList<Map<String, Object>>() {{ add(
                                new HashMap<String, Object>() {{
                                    put("args", new ArrayList<String>() {{ add((String) obj); }});
                                }}
                            );
                        }}
                );
            }
        } catch (Throwable e) {
            throw new InvalidArgumentException("" , e);
        }
    }

    @Override
    public T in(final IObject env)
            throws ReadValueException, InvalidArgumentException, ClassCastException {
        throw new InvalidArgumentException("Method not implemented.");
    }

    @Override
    public void out(final IObject env, final T in)
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

    private void setValueToNestedIObject(final IObject source, final String location, final Object value)
            throws ChangeValueException {
        try {
            String[] separated = location.split(SPLITTER);
            getNestedIObject(source, separated).setValue(
                    IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), separated[separated.length - 1]),
                    value
            );
        } catch (Throwable e) {
            throw new ChangeValueException("Could not write value to IObject", e);
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
