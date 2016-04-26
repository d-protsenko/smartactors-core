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
                "            return new Integer(object[0]);\n" +
                "        };\n" +
                "    }\n" +
                "}\n";

        String builderResult = CreateNewInstanceStrategyFactory.buildString("it.sevenbits.sandbox.bootstrap", "Func", "Integer", 1);
        assertEquals(testSample, builderResult);
    }

    @Test
    public void checkStringBuilderForSomePairsOfIncomingParams() {
        String testSample = "package it.sevenbits.sandbox.bootstrap;\n" +
                "import java.util.function.Function;\n" +
                "public class Func {\n" +
                "    public static Function<Object[], Object> createNewInstance() {\n" +
                "        return (Object[] object) -> {\n" +
                "            return new String(object[0], object[1], object[2]);\n" +
                "        };\n" +
                "    }\n" +
                "}\n";
        String builderResult = CreateNewInstanceStrategyFactory.buildString("it.sevenbits.sandbox.bootstrap", "Func", "String", 3);
        assertEquals(testSample, builderResult);
    }

    @Test
    public void checkStrategyCreation()
            throws StrategyFactoryException {
        IStrategyFactory factory = new CreateNewInstanceStrategyFactory();
        IResolveDependencyStrategy strategy = factory.createStrategy("", "it.sevenbits.sandbox.bootstrap", "Func", "String", 3);
        assertNotNull(strategy);
    }

    @Test (expected = StrategyFactoryException.class)
    public void checkStrategyCreationException()
            throws StrategyFactoryException {
        // Wrong arg "Str"
        Object[] funcParams = new Object[]{"it.sevenbits.sandbox.bootstrap", "Func", "Str", "byte[]", "int", "int"};
        Object[] factoryArgs = new Object[]{"", funcParams};
        IStrategyFactory factory = new CreateNewInstanceStrategyFactory();
        factory.createStrategy(factoryArgs);
    }
}
