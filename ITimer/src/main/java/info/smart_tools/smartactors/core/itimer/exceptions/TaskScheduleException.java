package info.smart_tools.smartactors.core.itimer.exceptions;

/**
 * Exception thrown by {@link info.smart_tools.smartactors.core.itimer.ITimer} when it cannot schedule a task because of any error.
 */
public class TaskScheduleException extends Exception {
    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param  message the detail message
     * @param  cause the cause
     */
    public TaskScheduleException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
