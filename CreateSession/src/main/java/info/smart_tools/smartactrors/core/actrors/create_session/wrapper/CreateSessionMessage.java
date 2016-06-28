package info.smart_tools.smartactrors.core.actrors.create_session.wrapper;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Wrapper for CreateSessionActor methods
 */
public interface CreateSessionMessage {

    /**
     * Returns current session
     * @return Session session or null
     * @throws ChangeValueException
     * @throws ReadValueException
     */
    Session getSession() throws ChangeValueException, ReadValueException;

    /**
     * Returns Authorization Info (example, device info)
     * @return IObject which contains auth info
     * @throws ChangeValueException
     * @throws ReadValueException
     */
    IObject getAuthInfo() throws ChangeValueException, ReadValueException;


    /**
     * set session in message
     * @param session Session
     * @throws ReadValueException
     * @throws ChangeValueException
     */
    void setSession(Session session) throws ReadValueException, ChangeValueException;
}
