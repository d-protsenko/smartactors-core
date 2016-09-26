package info.smart_tools.smartactors.actors.authentication.users.wrappers;

import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;

/**
 * Wrapper for constructor of {@link info.smart_tools.smartactors.actors.authentication.users.UserAuthByLoginActor}
 */
public interface IUserAuthByLoginParams {

    /**
     * @return collection name string
     * @throws ReadValueException if any error is occurred
     */
    String getCollection() throws ReadValueException;

    /**
     * @return connection pool
     * @throws ReadValueException if any error is occurred
     */
    IPool getConnectionPool() throws ReadValueException;

    /**
     * @return algorithm name
     * @throws ReadValueException if any error is occurred
     */
    String getAlgorithm() throws ReadValueException;

    /**
     * @return charset name
     * @throws ReadValueException if any error is occurred
     */
    String getCharset() throws ReadValueException;

    /**
     * @return encoder name
     * @throws ReadValueException if any error is occurred
     */
    String getEncoder() throws ReadValueException;
}
