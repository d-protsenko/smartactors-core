package info.smart_tools.smartactors.ioc_strategy_pack.resolve_by_type_strategy;

import info.smart_tools.smartactors.base.interfaces.istrategy.IStrategy;
import info.smart_tools.smartactors.base.interfaces.istrategy.exception.StrategyException;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ResolveByTypeStrategyTest {

    @Test
    public void checkStrategyCreation() {
        IStrategy strategy = new ResolveByTypeStrategy();
        assertNotNull(strategy);
    }

    @Test (expected = StrategyException.class)
    public void checkStrategyResolutionException()
            throws Exception {
        IStrategy strategy = new ResolveByTypeStrategy();
        strategy.resolve(null);
    }
}
