package info.smart_tools.smartactors.core.ienvironment_handler.exception;

/**
 * Exception for {@link info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler} that should be
 * thrown on internal endpoint error
 */
public class RequestHandlerInternalException extends Exception {

    /**
     * Constructor with specific error message
     * @param message specific error message
     */
    public RequestHandlerInternalException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */
    public RequestHandlerInternalException(final String message, final Throwable cause) {
        super(message, cause);
    }


    /**
     * Constructor with specific cause as arguments
     * @param cause specific cause
     */
    public RequestHandlerInternalException(final Throwable cause) {
        super(cause);
    }

}
