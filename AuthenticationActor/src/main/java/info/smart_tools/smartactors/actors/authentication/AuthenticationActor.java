package info.smart_tools.smartactors.actors.authentication;

import info.smart_tools.smartactors.actors.authentication.exception.AuthFailException;
import info.smart_tools.smartactors.actors.authentication.wrapper.AuthenticationMessage;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

import java.util.Objects;

/**
 * Actor that validate the created session
 */
public class AuthenticationActor {

    /**
     * Default constructor
     */
    public AuthenticationActor() {}

    /**
     * Validate the session
     * @param message the incoming message
     * @throws AuthFailException Throw when auth info is null or auth info is not equal with session auth info
     */
    public void authenticateSession(final AuthenticationMessage message) throws AuthFailException {
        try {
            String requestAuthInfo = message.getRequestUserAgent();
            String sessionAuthInfo = message.getSessionUserAgent();
            if (requestAuthInfo == null || !Objects.equals(requestAuthInfo, sessionAuthInfo)) {
                throw new AuthFailException("Failed to validate session: authentication info is incorrect");
            }
        } catch (ReadValueException e) {
            //Throw when one of parameters can't be read
            throw new AuthFailException("Failed to validate session: one of parameters can't be read", e);
        }
    }
}
