package info.smart_tools.smartactors.core.create_session.wrapper;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

public interface Session {
    /**
     * set authorization info into session
     * @param authInfo authorization info: device info etc
     * @throws ReadValueException
     * @throws ChangeValueException
     */
    public void setAuthInfo(IObject authInfo) throws ReadValueException, ChangeValueException;
}
