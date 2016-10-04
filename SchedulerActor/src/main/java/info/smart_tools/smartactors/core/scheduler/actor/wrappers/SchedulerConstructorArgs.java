package info.smart_tools.smartactors.core.scheduler.actor.wrappers;

import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

/**
 * Wrapper for scheduler actor constructor arguments.
 */
public interface SchedulerConstructorArgs {
    /**
     * Key name for connection options.
     *
     * @return key name
     * @throws ReadValueException if error occurs reading value from nested object
     */
    String getConnectionOptionsDependency() throws ReadValueException;

    /**
     * Key name for connection pool.
     *
     * @return key name
     * @throws ReadValueException if error occurs reading value from nested object
     */
    String getConnectionPoolDependency() throws ReadValueException;

    /**
     * Name of the collection where to store scheduler entries that should be saved.
     *
     * @return name of the collection
     * @throws ReadValueException if error occurs reading value from nested object
     */
    String getCollectionName() throws ReadValueException;
}
