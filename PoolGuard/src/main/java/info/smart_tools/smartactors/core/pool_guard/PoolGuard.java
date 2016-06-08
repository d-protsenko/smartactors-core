package info.smart_tools.smartactors.core.pool_guard;

import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.pool_guard.exception.PoolGuardException;

/**
 * Implementation of {@link IPoolGuard}
 */
public class PoolGuard implements IPoolGuard {
    private IPool pool;
    private Object currentObject;

    /**
     * Constructor for PoolGuard
     * @param pool the using pool
     */
    public PoolGuard(final IPool pool) {
        this.pool = pool;
    }


    /**
     * Get the free item from pool and remove him from pool,
     * or create the new instance if its possible
     * @throws  PoolGuardException if any errors occurred
     */
    public Object getObject() throws PoolGuardException {
        try {
            currentObject = pool.take();
            return currentObject;
        } catch (Exception e) {
            throw new PoolGuardException("PoolGuard could not get the free item", e);
        }
    }

    /**
     * Return object to pool if object is no needed more
     * @throws PoolGuardException if any errors occurred
     */
    public void close() throws PoolGuardException {
        try {
            if (currentObject != null) {
                pool.put(currentObject);
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new PoolGuardException("PoolGuard could not restore current item.", e);
        }
    }
}
