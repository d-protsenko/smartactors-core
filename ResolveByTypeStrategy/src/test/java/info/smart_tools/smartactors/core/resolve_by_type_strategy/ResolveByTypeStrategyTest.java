package info.smart_tools.smartactors.core.resolve_by_type_strategy;

import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class ResolveByTypeStrategyTest {

    @Test
    public void checkStrategyCreation() {
        IResolveDependencyStrategy strategy = new ResolveByTypeStrategy();
        assertNotNull(strategy);
    }

    @Test
    public void checkStrategyResolution()
            throws Exception{
        ResolveByTypeStrategy strategy = new ResolveByTypeStrategy();
        IKey key1 = new Key("key1");
        strategy.register(key1, mock(IResolveDependencyStrategy.class));
        IResolveDependencyStrategy result1 = strategy.resolve(key1);
        assertNotNull(result1);
        IKey key2 = new Key("key2");
        strategy.register(key2, mock(IResolveDependencyStrategy.class));
        IResolveDependencyStrategy result2 = strategy.resolve(key2);
        assertNotNull(result2);
        assertNotEquals(result1, result2);
    }

    @Test (expected = ResolveDependencyStrategyException.class)
    public void checkStrategyResolutionException()
            throws Exception {
        IResolveDependencyStrategy strategy = new ResolveByTypeStrategy();
        strategy.resolve(null);
    }
}
