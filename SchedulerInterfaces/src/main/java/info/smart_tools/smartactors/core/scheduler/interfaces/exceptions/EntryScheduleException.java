package info.smart_tools.smartactors.core.scheduler.interfaces.exceptions;

/**
 * Exception thrown by {@link info.smart_tools.smartactors.core.scheduler.interfaces.ISchedulerEntry schedule entry} when error occurs
 * scheduling/cancelling it.
 */
public class EntryScheduleException extends Exception {
    /**
     * The constructor.
     *
     * @param message    the message
     * @param cause      the cause
     */
    public EntryScheduleException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
