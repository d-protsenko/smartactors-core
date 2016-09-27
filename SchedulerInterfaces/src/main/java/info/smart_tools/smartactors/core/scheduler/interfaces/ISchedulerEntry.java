package info.smart_tools.smartactors.core.scheduler.interfaces;

import info.smart_tools.smartactors.core.scheduler.interfaces.exceptions.EntryStorageAccessException;
import info.smart_tools.smartactors.iobject.iobject.IObject;

/**
 * Interface of a scheduler entry. A entry consists of a message and a {@link ISchedulingStrategy scheduling strategy} that defines when to
 * send the message.
 */
public interface ISchedulerEntry {
    /**
     * Get the state object of this entry. The state object is an empty {@link IObject} for a just created entry and may be filled with any
     * fields by a {@link ISchedulingStrategy scheduling strategy}.
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
     */
    void cancel() throws EntryStorageAccessException;

    /**
     * Schedule this entry on given time.
     *
     * @param time    time (in milliseconds since epoch) to schedule this entry on
     */
    void scheduleNext(final long time);

    /**
     * Get the last time this entry was scheduled on. Returns {@code -1} if the entry is just created.
     *
     * @return the last time (in milliseconds since epoch) this entry was scheduled on
     */
    long getLastTime();

    /**
     * Create a {@link IObject} representing current state of this entry.
     *
     * @return the object representing current state of this entry
     */
    IObject toIObject();

    /**
     * @return identifier of this entry
     */
    String getId();
}
