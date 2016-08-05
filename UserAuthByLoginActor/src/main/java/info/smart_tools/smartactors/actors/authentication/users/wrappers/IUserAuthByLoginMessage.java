package info.smart_tools.smartactors.actors.authentication.users.wrappers;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

import javax.annotation.Nonnull;

/**
 * Wrapper for message with users credentials
 */
public interface IUserAuthByLoginMessage {

    /**
     * @return login
     * @throws ReadValueException if any error is occurred
     */
    String getLogin() throws ReadValueException;

    /**
     * @return raw password
     * @throws ReadValueException if any error is occurred
     */
    String getPassword() throws ReadValueException;

    /**
     * Sets authentication status
     * @param status text status
     * @throws ChangeValueException if any error is occurred
     */
    void setAuthStatus(@Nonnull final String status) throws ChangeValueException;

    /**
     * Sets message to respond
     * @param message text message
     * @throws ChangeValueException if any error is occurred
     */
    void setAuthMessage(@Nonnull final String message) throws ChangeValueException;
}
