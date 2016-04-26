package info.smart_tools.smartactors.core.create_new_instance_strategy;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IStrategyFactory;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.StrategyFactoryException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for CreateNewInstanceStrategyFactory
 */
public class CreateNewInstanceStrategyFactoryTest {

    @Test
    public void checkFactoryCreation() {
        IStrategyFactory factory = new CreateNewInstanceStrategyFactory();
        assertNotNull(factory);
    }

    @Test
    public void checkStringBuilderForOnePairOfIncomingParams() {
        String testSample = "package it.sevenbits.sandbox.bootstrap;\n" +
                "import java.util.function.Function;\n" +
                "public class Func {\n" +
                "    public static Function<Object[], Object> createNewInstance() {\n" +
                "        return (Object[] object) -> {\n" +
                "            return new Integer((int)(object[0]));\n" +
                "        };\n" +
                "    }\n" +
                "}\n";

        Object[] params = new Object[]{"it.sevenbits.sandbox.bootstrap", "Func", "Integer", "int"};
        String builderResult = CreateNewInstanceStrategyFactory.buildString(params);
        assertEquals(testSample, builderResult);
    }

    @Test
    public void checkStringBuilderForSomePairsOfIncomingParams() {
        String testSample = "package it.sevenbits.sandbox.bootstrap;\n" +
                "import java.util.function.Function;\n" +
                "public class Func {\n" +
                "    public static Function<Object[], Object> createNewInstance() {\n" +
                "        return (Object[] object) -> {\n" +
                "            return new String((byte[])(object[0]), (int)(object[1]), (int)(object[2]));\n" +
                "        };\n" +
                "    }\n" +
                "}\n";

        Object[] params = new Object[]{"it.sevenbits.sandbox.bootstrap", "Func", "String", "byte[]", "int", "int"};
        String builderResult = CreateNewInstanceStrategyFactory.buildString(params);
        assertEquals(testSample, builderResult);
    }

    @Test
    public void checkStrategyCreation()
            throws StrategyFactoryException {
        IStrategyFactory factory = new CreateNewInstanceStrategyFactory();
        Object[] factoryArgs = new Object[]{
                "", "it.sevenbits.sandbox.bootstrap", "Func", "String", "byte[]", "int", "int"
        };

        IResolveDependencyStrategy strategy = factory.createStrategy(factoryArgs);
        assertNotNull(strategy);
    }

    @Test (expected = StrategyFactoryException.class)
    public void checkStrategyCreationException()
            throws StrategyFactoryException {
        // Wrong arg "Str"
        Object[] factoryArgs = new Object[]{
                "", "it.sevenbits.sandbox.bootstrap", "Func", "Str", "byte[]", "int", "int"
        };
        IStrategyFactory factory = new CreateNewInstanceStrategyFactory();
        factory.createStrategy(factoryArgs);
    }
}
