package info.smart_tools.smartactors.actors.authentication.wrapper;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Wrapper for message for authentication actor
 */
public interface AuthenticationMessage {
    /**
     * @return auth information from session
     * @throws ReadValueException
     */
    IObject getAuthInfo() throws ReadValueException;

    /**
     * Set error to message if validation is failed
     * @param error the error message
     * @throws ChangeValueException
     */
    void setError(String error) throws ChangeValueException;
}
