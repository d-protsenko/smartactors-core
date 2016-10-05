package info.smart_tools.smartactors.core.scheduler.interfaces.exceptions;

/**
 * Exception thrown by {@link info.smart_tools.smartactors.core.scheduler.interfaces.ISchedulingStrategy scheduling strategy} when error
 * occurs.
 */
public class SchedulingStrategyExecutionException extends Exception {
    /**
     * The constructor.
     *
     * @param msg      the message
     * @param cause    the cause
     */
    public SchedulingStrategyExecutionException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /**
     * The constructor.
     *
     * @param msg    the message
     */
    public SchedulingStrategyExecutionException(final String msg) {
        this(msg, null);
    }
}
