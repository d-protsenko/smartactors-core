package info.smart_tools.smartactors.core.pool;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for ScopeGuard
 */

public class PoolTest {
    @Test
    public void Should_openConnectionsWhenRequired()
            throws Exception {
        Pool pool = new Pool(3, Object::new);
        assertNotNull(pool.tryTake());
        assertNotNull(pool.tryTake());
        assertNotNull(pool.tryTake());
    }

    @Test
    public void Should_returnConnectionSecondTime_WhenItIsReturnedToPool()
            throws Exception {
        Pool pool = new Pool(1, Object::new);
        pool.tryTake();
        pool.put(new Object());
        assertNotNull(pool.tryTake());
    }
}
