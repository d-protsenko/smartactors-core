package info.smart_tools.smartactors.actors.create_session.wrapper;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Wrapper for session
 */
public interface Session {
    /**
     * Set authorization info into session
     * @param authInfo authorization info: device info etc
     * @throws ChangeValueException Calling when try change value of variable
     */
    void setAuthInfo(IObject authInfo) throws ChangeValueException;
}
