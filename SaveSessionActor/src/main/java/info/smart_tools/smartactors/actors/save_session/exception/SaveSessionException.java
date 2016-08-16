package info.smart_tools.smartactors.actors.save_session.exception;

/**
 * Exception for {@link info.smart_tools.smartactors.actors.save_session.SaveSessionActor}
 */
public class SaveSessionException extends Exception {
    /**
     * Constructor with specific error message as argument
     * @param message specific error message
     */
    public SaveSessionException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */
    public SaveSessionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as argument
     * @param cause specific cause
     */
    public SaveSessionException(final Throwable cause) {
        super(cause);
    }
}
