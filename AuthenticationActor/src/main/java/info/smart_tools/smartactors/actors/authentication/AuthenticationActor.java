package info.smart_tools.smartactors.actors.authentication;

import info.smart_tools.smartactors.actors.authentication.wrapper.ActorParams;
import info.smart_tools.smartactors.actors.authentication.wrapper.AuthenticationMessage;
import info.smart_tools.smartactors.core.ds_object.FieldName;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;

/**
 * Actor that validate the created session
 */
public class AuthenticationActor {
    private static FieldName userAgentFieldName;

    /**
     * Constructor
     * @param params contains fiel name of validated object
     * @throws InvalidArgumentException
     */
    public AuthenticationActor(final ActorParams params) throws InvalidArgumentException {
        try {
            //TODO: get from IOC?
            userAgentFieldName = new FieldName(params.getUserAgentFieldName());
        } catch (ReadValueException e) {
            throw new InvalidArgumentException(e);
        }
    }

    /**
     * Validate the session
     * @param message the incoming message
     * @throws TaskExecutionException
     */
    public void authenticateSession(final AuthenticationMessage message) throws TaskExecutionException {
        try {
            IObject authInfo = message.getAuthInfo();
            String userAgent = (String) authInfo.getValue(userAgentFieldName);
            if (userAgent == null) {
                message.setError("Created session is broken");
            }
        } catch (ReadValueException | ChangeValueException | InvalidArgumentException e) {
            throw new TaskExecutionException("Failed to validate session", e);
        }
    }
}
