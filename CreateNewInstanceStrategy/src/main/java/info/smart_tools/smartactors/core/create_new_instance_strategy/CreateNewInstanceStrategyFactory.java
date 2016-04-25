package info.smart_tools.smartactors.core.create_new_instance_strategy;


import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IStrategyFactory;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.StrategyFactoryException;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * Implementation of {@link IStrategyFactory}
 * for {@link CreateNewInstanceStrategy}
 */
public class CreateNewInstanceStrategyFactory implements IStrategyFactory {

    /**
     * Minimum numbers of incoming args for creation class
     */
    private static final int MIN_ARGS_LENGTH = 3;

    /**
     * Create instance of {@link CreateNewInstanceStrategy}
     * <pre>
     *     For current implementation incoming object obj must have follow structure:
     *     {
     *         {@link String} - class path,
     *         {@link Object[]} - function parameters
     *     }
     *
     *     Function parameters must have follow structure:
     *     {
     *         {@link String} - package name,
     *         {@link String} - short name of class to be created,
     *         {@link String} - full name of class what will be created
     *             by instance of {@link IResolveDependencyStrategy},
     *         {@link String} - type of first argument,
     *         {@link String} - type of second argument,
     *         ...
     *         {@link String} - type of N argument
     *     }
     * </pre>
     * @param obj needed parameters for creation
     * @return new instance of {@link CreateNewInstanceStrategy}
     * @throws StrategyFactoryException if any errors occurred
     */
    public IResolveDependencyStrategy createStrategy(final Object obj)
            throws StrategyFactoryException {
        try {
            Object [] args = (Object[]) obj;
            Object classPath = args[0];
            Object[] funcParams = (Object[]) args[1];
            String sourceCode = buildString(funcParams);

            String fullClassName = funcParams[0] + "." + funcParams[1];
            Class<?> func = InMemoryCodeCompiler.compile((String) classPath, fullClassName, sourceCode);
            Method m = func.getDeclaredMethod("createNewInstance");
            Function<Object[], Object> f = (Function<Object[], Object>) m.invoke(null);

            return new CreateNewInstanceStrategy(f);
        } catch (Exception e) {
            throw new StrategyFactoryException("Failed to create instance of CreateNewInstanceStrategy.", e);
        }
    }

    /**
     * Build string with java code by given parameters
     * @param args list of needed parameters
     * @return string with java code
     */
    static String buildString(final Object ... args) {
        StringBuilder sourceCode = new StringBuilder();

        sourceCode.append("package "); sourceCode.append(args[0]); sourceCode.append(";\n");
        sourceCode.append("import java.util.function.Function;\n");

        sourceCode.append("public class "); sourceCode.append(args[1]); sourceCode.append(" {\n");
        sourceCode.append("    public static Function<Object[], Object> createNewInstance() {\n");
        sourceCode.append("        return (Object[] object) -> {\n");
        sourceCode.append("            return new ");
        sourceCode.append(args[2]); sourceCode.append("(");
        int length = args.length;
        for (int i = MIN_ARGS_LENGTH; i < length; i++) {
            sourceCode.append("(");
            sourceCode.append(args[i]);
            sourceCode.append(")");
            sourceCode.append("(");
            sourceCode.append("object[");
            sourceCode.append(i - MIN_ARGS_LENGTH);
            sourceCode.append("]");
            sourceCode.append(")");
            if (i + 1 < length) {
                sourceCode.append(", ");
            }
        }
        sourceCode.append(");\n");
        sourceCode.append("        };\n");
        sourceCode.append("    }\n");
        sourceCode.append("}\n");

        return sourceCode.toString();
    }
}
