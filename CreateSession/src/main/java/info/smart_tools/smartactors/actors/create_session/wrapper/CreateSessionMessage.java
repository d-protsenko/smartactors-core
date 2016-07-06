package info.smart_tools.smartactors.actors.create_session.wrapper;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Wrapper for CreateSessionActor methods
 */
public interface CreateSessionMessage {

    /**
     * Returns sessionId for current session
     * @return String that contain sessionId
     * @throws ReadValueException Calling when try read value of variable
     * @throws ChangeValueException Calling when try change value of variable
     */
    String getSessionId() throws ReadValueException, ChangeValueException;

    /**
     * Returns Authorization Info (example, device info)
     * @return IObject which contains auth info
     * @throws ChangeValueException Calling when try read value of variable
     * @throws ReadValueException Calling when try change value of variable
     */
    IObject getAuthInfo() throws ChangeValueException, ReadValueException;

    /**
     * Set session in message
     * @param session Session
     * @throws ReadValueException Calling when try read value of variable
     * @throws ChangeValueException Calling when try change value of variable
     */
    void setSession(Session session) throws ReadValueException, ChangeValueException;
}
