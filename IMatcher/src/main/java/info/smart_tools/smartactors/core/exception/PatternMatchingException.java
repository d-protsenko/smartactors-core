package info.smart_tools.smartactors.core.exception;

/**
 * Exception for {@link info.smart_tools.smartactors.core.imatcher.IMatcher}
 */
public class PatternMatchingException extends Exception {

    /**
     * Empty constructor for @PatternMatchingException
     */
    public PatternMatchingException() {
    }

    /**
     * @param message Message with exception
     */
    public PatternMatchingException(final String message) {
        super(message);
    }

    /**
     * @param message Message with exception
     * @param cause Cause of exception
     */
    public PatternMatchingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause Cause of exception
     */
    public PatternMatchingException(final Throwable cause) {
        super(cause);
    }
}
