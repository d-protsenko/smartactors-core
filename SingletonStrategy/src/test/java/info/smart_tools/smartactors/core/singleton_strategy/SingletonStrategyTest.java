package info.smart_tools.smartactors.core.singleton_strategy;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IStrategyFactory;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.StrategyFactoryException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link SingletonStrategy}
 */
public class SingletonStrategyTest {

    @Test
    public void checkStrategyResolution()
            throws StrategyFactoryException, ResolveDependencyStrategyException {
        IStrategyFactory factory = new SingletonStrategyFactory();

        String str = "abc";
        IResolveDependencyStrategy strategy = factory.createStrategy(str);
        String result1 = strategy.resolve();
        assertEquals("abc", result1);
        String result2 = strategy.resolve();
        assertEquals("abc", result2);
        assertEquals(result1, result2);
    }

}
