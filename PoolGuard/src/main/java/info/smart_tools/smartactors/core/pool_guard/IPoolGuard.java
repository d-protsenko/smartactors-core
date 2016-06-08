package info.smart_tools.smartactors.core.pool_guard;

import info.smart_tools.smartactors.core.pool_guard.exception.PoolGuardException;

/**
 * PoolGuard interface
 * provides methods for switch current {@link info.smart_tools.smartactors.core.ipool.IPool}
 * by other
 * @since 1.7+
 */
public interface IPoolGuard extends AutoCloseable {

    /**
     * Get the free item from pool and remove him from pool,
     * or create the new instance if its possible
     * @throws  PoolGuardException if any errors occurred
     */
    Object getObject() throws PoolGuardException;

    /**
     * Return object to pool if object is no needed more
     * @throws  PoolGuardException if any errors occurred
     */
    void close() throws PoolGuardException;
}
