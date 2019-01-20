package info.smart_tools.smartactors.ioc.resolve_by_name_ioc_strategy;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for ResolveByNameIocStrategy
 */
public class ResolveByNameIocStrategyTest {

    @Test
    public void checkStrategyCreation() {
        IResolutionStrategy strategy = new ResolveByNameIocStrategy();
        assertNotNull(strategy);
    }

    @Test
    public void checkStrategyResolution()
            throws Exception{
        IResolutionStrategy strategy = new ResolveByNameIocStrategy();
        IKey key1 = strategy.resolve("unique_key");
        assertNotNull(key1);
        IKey key2 = strategy.resolve("unique_key");
        assertNotNull(key2);
        assertEquals(key1, key2);
        IKey key3 = strategy.resolve("unique_key_other");
        assertNotEquals(key1, key3);
    }

    @Test (expected = ResolutionStrategyException.class)
    public void checkStrategyResolutionException()
            throws Exception {
        IResolutionStrategy strategy = new ResolveByNameIocStrategy();
        strategy.resolve(null);
    }
}
