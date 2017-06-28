package info.smart_tools.smartactors.async_operations.close_async_operation.exception;

public class CloseAsyncOpException extends Exception {
    /**
     * Constructor with specific error message as argument
     * @param message specific error message
     */
    public CloseAsyncOpException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */
    public CloseAsyncOpException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as argument
     * @param cause specific cause
     */
    public CloseAsyncOpException(final Throwable cause) {
        super(cause);
    }
}
