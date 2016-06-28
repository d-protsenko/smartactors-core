package info.smart_tools.smartactrors.core.actrors.create_session.wrapper;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Wrapper for session
 */
public interface Session {
    /**
     * set authorization info into session
     * @param authInfo authorization info: device info etc
     * @throws ReadValueException
     * @throws ChangeValueException
     */
    void setAuthInfo(IObject authInfo) throws ReadValueException, ChangeValueException;
}
