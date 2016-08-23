package info.smart_tools.smartactors.strategy.uuid_nextid_strategy;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for UUID strategy.
 */
public class UuidNextIdStrategyTest {

    @Test
    public void testUuidGenerated() throws ResolveDependencyStrategyException {
        IResolveDependencyStrategy strategy = new UuidNextIdStrategy();
        assertTrue(strategy.resolve() instanceof String);
        assertNotEquals(strategy.resolve(), strategy.resolve());
    }

}
