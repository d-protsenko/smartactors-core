package info.smart_tools.smartactors.core.async_operation_collection.exception;

/**
 * Exception for error in {@link info.smart_tools.smartactors.core.async_operation_collection.IAsyncOperationCollection}
 * getAsyncOperation method
 */
public class GetAsyncOperationException extends Exception {

    /**
     * Constructor with specific error message as argument
     * @param message specific error message
     */
    public GetAsyncOperationException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */
    public GetAsyncOperationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}