package info.smart_tools.smartactors.actor.change_password.wrapper;

import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Wrapper for {@link info.smart_tools.smartactors.actor.change_password.ChangePasswordActor} handler
 */
public interface ChangePasswordMessage {

    /**
     * Getter
     * @return user identifier
     * @throws ReadValueException if error during get is occurred
     */
    String getUserId() throws ReadValueException;

    /**
     * Getter
     * @return new user password
     * @throws ReadValueException if error during get is occurred
     */
    String getPassword() throws ReadValueException;
}
