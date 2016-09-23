package info.smart_tools.smartactors.actor.change_password.wrapper;

import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;

/**
 * Wrapper for constructor of {@link info.smart_tools.smartactors.actor.change_password.ChangePasswordActor}
 */
public interface ChangePasswordConfig {

    /**
     * Getter
     * @return wrapped collection name
     * @throws ReadValueException if error during get is occurred
     */
    String getCollectionName() throws ReadValueException;

    /**
     * Getter
     * @return connection pool
     * @throws ReadValueException if error during get is occurred
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
