package info.smart_tools.smartactors.core.create_new_instance_strategy;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for CreateNewInstanceStrategy
 */
public class CreateNewInstanceStrategyTest {

//    @Test
//    public void checkCreateNewInstanceStrategyCreation() {
//        Object[] args = new Object[]{
//                "", "it.sevenbits.sandbox.bootstrap", "Func", "String", "byte[]", "int", "int"
//        };
//        IResolveDependencyStrategy strategy = new CreateNewInstanceStrategy(args);
//        assertNotNull(strategy);
//    }
//
//    @Test (expected = IllegalArgumentException.class)
//    public void checkStrategyCreationException()
//            throws Exception {
//        // Wrong arg "Str"
//        Object[] args = new Object[]{
//                "", "it.sevenbits.sandbox.bootstrap", "Func", "Str", "byte[]", "int", "int"
//        };
//        IResolveDependencyStrategy strategy = new CreateNewInstanceStrategy(args);
//    }
//
//
//    @Test
//    public void checkStrategyResolution()
//            throws Exception {
//        Object[] factoryArgs = new Object[]{
//                "", "it.sevenbits.sandbox.bootstrap", "Func", "String", "byte[]", "int", "int"
//        };
//
//        IResolveDependencyStrategy strategy = new CreateNewInstanceStrategy(factoryArgs);
//        Object[] objectArgs = new Object[]{new byte[]{'a','b','c'}, 1, 2};
//        String result = strategy.resolve(objectArgs);
//        assertEquals("bc", result);
//    }
//
//    @Test (expected = ResolveDependencyStrategyException.class)
//    public void checkStrategyResolutionException()
//            throws Exception {
//        Object[] factoryArgs = new Object[]{
//                "", "it.sevenbits.sandbox.bootstrap", "Func", "String", "byte[]", "int", "int"
//        };
//        IResolveDependencyStrategy strategy = new CreateNewInstanceStrategy(factoryArgs);
//        // Absent one arg in objectArgs
//        Object[] objectArgs = new Object[]{new byte[]{'a','b','c'}, 1};
//        String result = strategy.resolve(objectArgs);
//    }
}
