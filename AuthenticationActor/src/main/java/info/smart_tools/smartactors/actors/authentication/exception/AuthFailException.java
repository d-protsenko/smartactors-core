package info.smart_tools.smartactors.actors.authentication.exception;

/**
 * Exception for error in {@link info.smart_tools.smartactors.actors.authentication}
 */
public class AuthFailException extends Exception {

    /**
     * Constructor with specific error message as argument
     * @param message specific error message
     */
    public AuthFailException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */
    public AuthFailException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as argument
     * @param cause specific cause
     */
    public AuthFailException(final Throwable cause) {
        super(cause);
    }
}
