package info.smart_tools.smartactors.ioc.strategy_container;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.ioc.istrategy_container.IStrategyContainer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

/**
 * Tests for Strategy
 */
public class StrategyContainerTest {

    @Test
    public void checkRegistrationResolutionDeletion()
            throws Exception {
        IStrategyContainer container = new StrategyContainer();
        IResolutionStrategy strategy = mock(IResolutionStrategy.class);
        Object key = new Object();
        container.register(key, strategy);
        IResolutionStrategy result = container.resolve(key);
        assertEquals(result, strategy);
        container.remove(key);
        result = container.resolve(key);
        assertNull(result);
        reset(strategy);
    }
}
