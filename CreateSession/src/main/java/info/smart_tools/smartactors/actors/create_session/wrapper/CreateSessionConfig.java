package info.smart_tools.smartactors.actors.create_session.wrapper;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ipool.IPool;

/**
 * Contains params for CreateSessionActor
 */
public interface CreateSessionConfig {

    /**
     *
     * @throws ReadValueException
     * @throws ChangeValueException
     */
    String getCollectionName() throws ReadValueException, ChangeValueException;

    /**
     *
     * @throws ReadValueException
     * @throws ChangeValueException
     */
    IPool getConnectionPool() throws ReadValueException, ChangeValueException;
}
