package info.smart_tools.smartactors.base.interfaces.ipool;

import info.smart_tools.smartactors.base.interfaces.ipool.exception.PoolPutException;
import info.smart_tools.smartactors.base.interfaces.ipool.exception.PoolTakeException;

/**
 * Pool interface
 */
public interface IPool {
    /**
     * Get an item from the pool
     * @return found object
     * @throws PoolTakeException if value is not found or any error occurred
     */
    Object take() throws PoolTakeException;

    /**
     * Stores an value to the pool after using
     * @param value given value
     * @throws PoolPutException any if error occurred
     */
    void put(final Object value) throws PoolPutException;
}
