package info.smart_tools.smartactors.scheduler.interfaces.exceptions;

import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;

/**
 * Exception thrown by {@link ISchedulerEntry} when it cannot save itself to a
 * storage (database).
 */
public class EntryStorageAccessException extends Exception {
    /**
     * The constructor.
     *
     * @param msg      the exception message
     * @param cause    cause
     */
    public EntryStorageAccessException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /**
     * The constructor.
     *
     * @param msg      the exception message
     */
    public EntryStorageAccessException(final String msg) {
        super(msg);
    }
}
