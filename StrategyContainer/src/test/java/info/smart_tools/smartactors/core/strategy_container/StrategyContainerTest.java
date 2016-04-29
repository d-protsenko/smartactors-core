package info.smart_tools.smartactors.core.strategy_container;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.istrategy_container.IStrategyContainer;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

/**
 * Tests for Strategy
 */
public class StrategyContainerTest {

    @Test
    public void checkResolution()
            throws Exception {
        IStrategyContainer container = new StrategyContainer();
        Map<Object, IResolveDependencyStrategy> strategyStorage = new ConcurrentHashMap<Object, IResolveDependencyStrategy>();
        IResolveDependencyStrategy strategy = mock(IResolveDependencyStrategy.class);
        Object key = new Object();
        strategyStorage.put(key, strategy);
        Field field = container.getClass().getDeclaredField("strategyStorage");
        field.setAccessible(true);
        field.set(container, strategyStorage);
        field.setAccessible(false);

        IResolveDependencyStrategy result = container.resolve(key);
        assertEquals(result, strategy);
        reset(strategy);
    }

    @Test
    public void checkRegistration()
            throws Exception {
        IStrategyContainer container = new StrategyContainer();
        Map<Object, IResolveDependencyStrategy> strategyStorage = new ConcurrentHashMap<Object, IResolveDependencyStrategy>();
        IResolveDependencyStrategy strategy = mock(IResolveDependencyStrategy.class);
        Object key = new Object();
        Field field = container.getClass().getDeclaredField("strategyStorage");
        field.setAccessible(true);
        field.set(container, strategyStorage);
        field.setAccessible(false);
        assertEquals(strategyStorage.size(), 0);
        container.register(key, strategy);
        assertEquals(strategyStorage.get(key), strategy);
        assertEquals(strategyStorage.size(), 1);
        reset(strategy);
    }

    @Test
    public void checkDeletion()
            throws Exception {
        IStrategyContainer container = new StrategyContainer();
        Map<Object, IResolveDependencyStrategy> strategyStorage = new ConcurrentHashMap<Object, IResolveDependencyStrategy>();
        IResolveDependencyStrategy strategy = mock(IResolveDependencyStrategy.class);
        Object key = new Object();
        strategyStorage.put(key, strategy);
        Field field = container.getClass().getDeclaredField("strategyStorage");
        field.setAccessible(true);
        field.set(container, strategyStorage);
        field.setAccessible(false);
        assertEquals(strategyStorage.size(), 1);
        container.remove(key);
        assertEquals(strategyStorage.size(), 0);
        reset(strategy);
    }
}
