package info.smart_tools.smartactors.ioc_strategy_pack.resolve_by_type_strategy;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ResolveByTypeStrategyTest {

    @Test
    public void checkStrategyCreation() {
        IResolutionStrategy strategy = new ResolveByTypeStrategy();
        assertNotNull(strategy);
    }

    @Test (expected = ResolutionStrategyException.class)
    public void checkStrategyResolutionException()
            throws Exception {
        IResolutionStrategy strategy = new ResolveByTypeStrategy();
        strategy.resolve(null);
    }
}
