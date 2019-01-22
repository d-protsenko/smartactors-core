package info.smart_tools.smartactors.ioc_strategy_pack.uuid_nextid_strategy;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for UUID strategy.
 */
public class UuidNextIdStrategyTest {

    @Test
    public void testUuidGenerated() throws ResolutionStrategyException {
        IResolutionStrategy strategy = new UuidNextIdStrategy();
        assertTrue(strategy.resolve() instanceof String);
        assertNotEquals(strategy.resolve(), strategy.resolve());
    }

}
