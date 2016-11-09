package info.smart_tools.smartactors.async_operations.check_validity_async_operation.exception;

import info.smart_tools.smartactors.async_operations.check_validity_async_operation.CheckValidityAsyncOperationActor;

/**
 * Exception for error in {@link CheckValidityAsyncOperationActor} methods
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
