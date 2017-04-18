package info.smart_tools.smartactors.base.strategy.strategy_storage_strategy;

import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.IAdditionDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StrategyStorageStrategyTest {

    @Test
    public void checkInstanceCreation()
            throws Exception {
        IResolveDependencyStrategy sss = new StrategyStorageStrategy();
        assertNotNull(sss);
    }

    @Test
    public void checkResolution()
            throws Exception {
        String value = "result";
        IResolveDependencyStrategy innerStrategy = mock(IResolveDependencyStrategy.class);
        when(innerStrategy.resolve(new Object[] {"new", "arg"})).thenReturn(value);

        IResolveDependencyStrategy sss = new StrategyStorageStrategy();
        ((IAdditionDependencyStrategy) sss).register("new", innerStrategy);

        String result = sss.resolve("new", "arg");
        assertEquals(value, result);

        ((IAdditionDependencyStrategy) sss).remove("new");

        result = sss.resolve("new", "arg");
        assertNull(result);
    }

    @Test (expected = ResolveDependencyStrategyException.class)
    public void checkResolutionException()
            throws Exception {
        IResolveDependencyStrategy sss = new StrategyStorageStrategy();
        sss.resolve(null);
    }
}
