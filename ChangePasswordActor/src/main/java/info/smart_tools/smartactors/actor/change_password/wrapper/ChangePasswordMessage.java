package info.smart_tools.smartactors.actor.change_password.wrapper;

import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

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

    /**
     * Sets authentication status
     * @param status text status
     * @throws ChangeValueException if any error is occurred
     */
    void setAuthStatus(final String status) throws ChangeValueException;

    /**
     * Sets message to respond
     * @param message text message
     * @throws ChangeValueException if any error is occurred
     */
    void setAuthMessage(final String message) throws ChangeValueException;
}
