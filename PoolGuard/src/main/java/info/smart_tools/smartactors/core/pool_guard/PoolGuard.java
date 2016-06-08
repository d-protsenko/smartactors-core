package info.smart_tools.smartactors.core.pool_guard;

import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.pool_guard.exception.PoolGuardException;

public class PoolGuard implements IPoolGuard{
    /**
     * Local storage for instance of {@link IPool}
     */
    private IPool pool;


    /**
     * Get the free item from pool and remove him from pool,
     * or create the new instance if its possible
     * @throws  PoolGuardException if any errors occurred
     */
    public Object getObject() throws PoolGuardException {
        try {
            return pool.tryTake();
        } catch (Exception e) {
            throw new PoolGuardException(e);
        }
    }

    /**
     * Return object to pool if object is no needed more
     * @throws PoolGuardException if any errors occurred
     */
    public void close() throws PoolGuardException {

    }
}
