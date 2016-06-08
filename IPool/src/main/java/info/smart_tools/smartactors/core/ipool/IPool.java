package info.smart_tools.smartactors.core.ipool;

import info.smart_tools.smartactors.core.ipool.exception.PoolException;

/**
 * Pool interface
 */
public interface IPool {
    /**
     * Get an item from the pool
     * @return found object
     * @throws PoolException if value is not found or any error occurred
     */
    Object tryTake() throws PoolException;

    /**
     * Stores an value to the pool after using
     * @param value given value
     * @throws PoolException any if error occurred
     */
    void put(final Object value) throws PoolException;
}
