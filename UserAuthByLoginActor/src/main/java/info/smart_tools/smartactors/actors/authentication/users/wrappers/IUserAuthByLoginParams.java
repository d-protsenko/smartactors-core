package info.smart_tools.smartactors.actors.authentication.users.wrappers;

import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ipool.IPool;

/**
 *
 */
public interface IUserAuthByLoginParams {
    /**
     *
     * @return
     */
    String getCollection() throws ReadValueException;

    /**
     * @return connection pool
     * @throws ReadValueException if any error is occurred
     */
    IPool getConnectionPool() throws ReadValueException;

    /**
     *
     * @return
     */
    String getAlgorithm() throws ReadValueException;

    /**
     *
     * @return
     */
    String getCharset() throws ReadValueException;

    /**
     *
     * @return
     */
    String getEncoder() throws ReadValueException;
}
