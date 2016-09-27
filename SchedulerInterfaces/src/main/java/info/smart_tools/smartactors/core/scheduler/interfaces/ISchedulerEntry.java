package info.smart_tools.smartactors.core.scheduler.interfaces;

import info.smart_tools.smartactors.core.scheduler.interfaces.exceptions.EntryScheduleException;
import info.smart_tools.smartactors.core.scheduler.interfaces.exceptions.EntryStorageAccessException;
import info.smart_tools.smartactors.iobject.iobject.IObject;

/**
 * Interface of a scheduler entry. A entry consists of a message and a {@link ISchedulingStrategy scheduling strategy} that defines when to
 * send the message.
 *
 * <p>
 *     When stored in database a entry is represented by a object like the following:
 * </p>
 *
 * <pre>
 *     {
 *         "entryId": "5cb03e18-e86a-4729-a281-e539fe2c2291",        // Unique identifier of the entry
 *         "strategy": "continuously repeat scheduling strategy",    // Dependency name of the {@link ISchedulingStrategy
 *                                                                      scheduling strategy}
 *         "message": {. . .},                                       // The message to send
 *
 *         . . .                                                     // Fields specific for scheduling strategy
 *     }
 * </pre>
 */
public interface ISchedulerEntry {
    /**
     * Get the state object of this entry.
     *
     * <p>
     *     Should contain at least the following fields:
     * </p>
     * <ul>
     *     <li>{@code "strategy"} - dependency name of the strategy used to determine time(s) when to send the message</li>
     *     <li>{@code "message"} - message to send</li>
     *     <li>{@code "entryId"} - identifier of the entry</li>
     * </ul>
     * <p>
     *     Field names ending with {@code "ID"} are reserved for storage primary keys. {@link ISchedulingStrategy Scheduling strategy} may
     *     use any other fields to store specific values.
     * </p>
     *
     * @return state of this entry
     */
    IObject getState();

    /**
     * Save this entry in database.
     *
     * @throws EntryStorageAccessException if cannot save the entry to database
     */
    void save() throws EntryStorageAccessException;

    /**
     * Cancel this entry. Will delete entry from database if it was saved in there.
     *
     * @throws EntryStorageAccessException if cannot delete the entry from database
     * @throws EntryScheduleException if error occurs cancelling the entry
     */
    void cancel() throws EntryStorageAccessException, EntryScheduleException;

    /**
     * Schedule this entry on given time.
     *
     * @param time    time (in milliseconds since epoch) to schedule this entry on
     * @throws EntryScheduleException if error occurs scheduling entry
     */
    void scheduleNext(final long time) throws EntryScheduleException;

    /**
     * Get the last time this entry was scheduled on. Returns {@code -1} if the entry is just created or restored from state saved in
     * database.
     *
     * @return the last time (in milliseconds since epoch) this entry was scheduled on
     */
    long getLastTime();

    /**
     * @return identifier of this entry
     */
    String getId();
}
