package info.smart_tools.smartactors.core.singleton_strategy;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IStrategyFactory;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.StrategyFactoryException;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Tests for {@link SingletonStrategyFactory}
 */

public class SingletonStrategyFactoryTest {

    @Test
    public void checkSingletonStrategyFactoryCreation() {
        IStrategyFactory factory = new SingletonStrategyFactory();
        assertNotNull(factory);
    }

    @Test
    public void checkStrategyCreation()
            throws StrategyFactoryException, ResolveDependencyStrategyException {
        IStrategyFactory factory = new SingletonStrategyFactory();

        String str = "abc";
        IResolveDependencyStrategy strategy1 = factory.createStrategy(str);
        assertNotNull(strategy1);
    }

    @Test (expected = StrategyFactoryException.class)
    public void checkExceptionOnCreation()
            throws StrategyFactoryException {
        IStrategyFactory factory = new SingletonStrategyFactory();

        factory.createStrategy(null);
        fail();
    }
}