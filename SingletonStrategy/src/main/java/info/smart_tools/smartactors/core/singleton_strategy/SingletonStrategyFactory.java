package info.smart_tools.smartactors.core.singleton_strategy;

import info.smart_tools.smartactors.core.class_storage.ClassStorage;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IStrategyFactory;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.StrategyFactoryException;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link IStrategyFactory}
 * for {@link SingletonStrategy}
 */
public class SingletonStrategyFactory implements IStrategyFactory {

    private static Map<String, Class> mapPrimitives = new HashMap<String, Class>(8);
    static {
        mapPrimitives.put("Integer", Integer.class);
        mapPrimitives.put("int", int.class);
        mapPrimitives.put("Long", Long.class);
        mapPrimitives.put("long", long.class);
        mapPrimitives.put("Float", Float.class);
        mapPrimitives.put("float", float.class);
        mapPrimitives.put("Double", Double.class);
        mapPrimitives.put("double", double.class);
        mapPrimitives.put("Byte", Byte.class);
        mapPrimitives.put("byte", byte.class);
        mapPrimitives.put("Boolean", Boolean.class);
        mapPrimitives.put("boolean", boolean.class);
        mapPrimitives.put("Character", Character.class);
        mapPrimitives.put("char", char.class);
        mapPrimitives.put("Short", Short.class);
        mapPrimitives.put("short", short.class);
    }

    /**
     * Create instance of {@link SingletonStrategy} using reflection
     * <pre>
     *     For current implementation incoming object obj must have follow structure:
     *     {
     *         {@link String} - class_id,
     *         {@link Object[]} - parameters for creation instance of class
     *     }
     *
     *     Parameters for creation instance of class must have follow structure:
     *     {
     *         {@link String} - type of first argument,
     *         {@link String} - value of first argument,
     *         {@link String} - type of second argument,
     *         {@link String} - value of second argument,
     *         ...
     *         {@link String} - type of N argument,
     *         {@link String} - value of N argument
     *     }
     * </pre>

     * @param obj needed parameters for creation
     * @return new instance of {@link SingletonStrategy}
     * @throws StrategyFactoryException if any errors occurred
     */
    public IResolveDependencyStrategy createStrategy(final Object obj)
            throws StrategyFactoryException {
        try {
            Object[] args = (Object[]) obj;
            Object classId = args[0];
            Object[] classParams = (Object[]) args[1];
            Class<?> clazz = ClassStorage.getClass(classId);
            Class[] classes = resolveClassTypes(classParams);
            Object[] values = resolveClassArgs(classParams);
            Object instance = clazz.getDeclaredConstructor(classes).newInstance(values);

            return new SingletonStrategy(instance);
        } catch (Exception e) {
            throw new StrategyFactoryException("Failed to create instance of SingletonStrategy.", e);
        }
    }

    private Class[] resolveClassTypes(final Object[] params)
            throws Exception {
        Class[] classes = new Class[params.length / 2];
        for (int i = 0; i < params.length - 1; i += 2) {
            String param = (String) params[i];
            boolean isArray = false;
            if (param.contains("[")) {
                isArray = true;
                int endIndex = param.indexOf('[');
                param = param.substring(0, endIndex);
            }
            Class<?> clazz = mapPrimitives.get(param);
            if (clazz == null) {
                clazz = ClassStorage.getClass(param);
            }

            classes[i / 2] = isArray ? Array.newInstance(clazz, 1).getClass() : clazz;
        }

        return classes;
    }

    private Object[] resolveClassArgs(final Object[] params) {
        Object[] args = new Object[ params.length / 2];
        for (int i = 1; i <=  params.length - 1; i += 2) {
            args[(i - 1) / 2] = params[i];
        }

        return args;
    }
}
