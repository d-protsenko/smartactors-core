package info.smart_tools.smartactors.core.create_new_instance_strategy;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IStrategyFactory;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.StrategyFactoryException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for CreateNewInstanceStrategy
 */
public class CreateNewInstanceStrategyTest {

    @Test
    public void checkStrategyCreation()
            throws StrategyFactoryException, ResolveDependencyStrategyException {
        IStrategyFactory factory = new CreateNewInstanceStrategyFactory();
        Object[] factoryArgs = new Object[]{
                "", "it.sevenbits.sandbox.bootstrap", "Func", "String", "byte[]", "int", "int"
        };

        IResolveDependencyStrategy strategy = factory.createStrategy(factoryArgs);
        Object[] objectArgs = new Object[]{new byte[]{'a','b','c'}, 1, 2};
        String result = strategy.resolve(objectArgs);
        assertEquals("bc", result);
    }

    @Test (expected = ResolveDependencyStrategyException.class)
    public void checkStrategyCreationException()
            throws StrategyFactoryException, ResolveDependencyStrategyException {
        Object[] factoryArgs = new Object[]{
                "", "it.sevenbits.sandbox.bootstrap", "Func", "String", "byte[]", "int", "int"
        };
        IStrategyFactory factory = new CreateNewInstanceStrategyFactory();
        IResolveDependencyStrategy strategy = factory.createStrategy(factoryArgs);
        // Absent one arg in objectArgs
        Object[] objectArgs = new Object[]{new byte[]{'a','b','c'}, 1};
        String result = strategy.resolve(objectArgs);
    }
}
