package info.smart_tools.smartactors.core.exception;

/**
 * Exception thrown in case of error occurred in process of task execution.
 */
//TODO:: remove this dup, when ITask would be merged into develop
public class TaskExecutionException extends Exception {
    /**
     * Constructor with specific error message as argument
     * @param message specific error message
     */
    public TaskExecutionException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */

    public TaskExecutionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as argument
     * @param cause specific cause
     */
    public TaskExecutionException(final Throwable cause) {
        super(cause);
    }
}