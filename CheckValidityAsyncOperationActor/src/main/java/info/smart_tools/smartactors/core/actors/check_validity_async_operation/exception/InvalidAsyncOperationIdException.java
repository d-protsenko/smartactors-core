package info.smart_tools.smartactors.core.actors.check_validity_async_operation.exception;

/**
 * Exception for error in {@link info.smart_tools.smartactors.core.actors.check_validity_async_operation.CheckValidityAsyncOperationActor} methods
 */
public class InvalidAsyncOperationIdException extends Exception {

    /**
     * Constructor with specific error message as argument
     * @param message specific error message
     */
    public InvalidAsyncOperationIdException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */
    public InvalidAsyncOperationIdException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as argument
     * @param cause specific cause
     */
    public InvalidAsyncOperationIdException(final Throwable cause) {
        super(cause);
    }
}
