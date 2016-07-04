package info.smart_tools.smartactors.actor.change_password.wrapper;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;

/**
 * User wrapper
 */
public interface User {

    /**
     * Setter
     * @param password new user's password
     * @throws ChangeValueException if error during set is occurred
     */
    void setPassword(String password) throws ChangeValueException;
}
