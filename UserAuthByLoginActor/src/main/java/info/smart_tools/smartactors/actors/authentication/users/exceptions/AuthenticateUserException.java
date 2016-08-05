package info.smart_tools.smartactors.actors.authentication.users.exceptions;

/**
 * Exception for errors occurred during auth user
 */
public class AuthenticateUserException extends Exception {
    /**
     * Constructor with specific error message as argument
     * @param message specific error message
     */
    public AuthenticateUserException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */
    public AuthenticateUserException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
