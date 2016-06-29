package info.smart_tools.smartactors.core.idatabase_task.exception;

/**
 * Throws when initialize task error.
 */
public class TaskInitializationException extends Exception {
    /**
     * Constructor with specific error message as argument
     *
     * @param message - specific error message.
     */
    public TaskInitializationException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     *
     * @param message specific error message
     * @param cause specific cause
     */
    public TaskInitializationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
