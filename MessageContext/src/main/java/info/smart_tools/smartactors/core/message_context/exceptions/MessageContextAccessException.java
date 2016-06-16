package info.smart_tools.smartactors.core.message_context.exceptions;

/**
 * Exception thrown by {@link info.smart_tools.smartactors.core.message_context.MessageContext} and {@link
 * info.smart_tools.smartactors.core.message_context.IMessageContextContainer}.
 */
public class MessageContextAccessException extends Exception {
    /**
     * Constructor with specific error message as argument
     * @param message specific error message
     */
    public MessageContextAccessException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */

    public MessageContextAccessException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as argument
     * @param cause specific cause
     */
    public MessageContextAccessException(final Throwable cause) {
        super(cause);
    }
}
