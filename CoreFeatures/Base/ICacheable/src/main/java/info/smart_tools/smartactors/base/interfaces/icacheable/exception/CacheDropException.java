package info.smart_tools.smartactors.base.interfaces.icacheable.exception;

/**
 * Exception for error in {@link info.smart_tools.smartactors.base.interfaces.icacheable.ICacheable} methods
 */

public class CacheDropException extends Exception {

    /**
     * Constructor with specific error message as argument
     * @param message specific error message
     */
    public CacheDropException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */
    public CacheDropException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as argument
     * @param cause specific cause
     */
    public CacheDropException(final Throwable cause) {
        super(cause);
    }
}
