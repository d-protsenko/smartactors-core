package info.smart_tools.smartactors.core.scheduler.interfaces;

import info.smart_tools.smartactors.core.scheduler.interfaces.exceptions.EntryStorageAccessException;

import java.util.List;

/**
 * Provides local (in-memory) and remote (database) storage for {@link ISchedulerEntry scheduler entries}.
 */
public interface ISchedulerEntryStorage {
    /**
     * Save the entry to both local and remote storage.
     *
     * @param entry    the entry to save
     * @throws EntryStorageAccessException if error occurs accessing remote storage
     */
    void save(final ISchedulerEntry entry) throws EntryStorageAccessException;

    /**
     * Save the entry to local storage.
     *
     * @param entry    the entry to save
     * @throws EntryStorageAccessException if error occurs
     */
    void saveLocally(final ISchedulerEntry entry) throws EntryStorageAccessException;

    /**
     * Delete the given entry from both local and remote storage.
     *
     * <p>
     * <b>IMPORTANT:</b> this method should be called only by entry implementation's {@link ISchedulerEntry#cancel() #cancel()} method. When
     * called from outside the result is undefined: the entry may remain active (if it saves itself on next execution) or remain active only
     * until server shutdown (when it does not). To delete and cancel the entry call {@link ISchedulerEntry#cancel() #cancel()} method of
     * the entry itself.
     * </p>
     *
     * @param entry    the entry
     * @throws EntryStorageAccessException if error occurs accessing remote storage
     */
    void delete(final ISchedulerEntry entry) throws EntryStorageAccessException;

    /**
     * Get list of all locally saved entries.
     *
     * @return list of all locally saved entries
     * @throws EntryStorageAccessException if any error occurs
     */
    List<ISchedulerEntry> listLocalEntries() throws EntryStorageAccessException;

    /**
     * Get (a locally saved) entry with given identifier.
     *
     * @param id    the entry identifier
     * @return the entry saved with given identifier
     * @throws EntryStorageAccessException if there is no entry with given identifier
     */
    ISchedulerEntry getEntry(final String id) throws EntryStorageAccessException;
    /**
     * Download next page of entries from remote storage.
     *
     * <p>
     * This method should be called continuously (until returns {@code true}) before any other method may be used safely. It will download
     * entries saved in remote storage (database) page by page.
     * </p>
     *
     * <p>
     * May be called from different threads (but from not more than one at time).
     * </p>
     *
     * @param preferSize preferred size of page {@code 0} or negative number is for default size
     * @return {@code true} if there is no more entries to download
     * @throws EntryStorageAccessException if any error occurs
     */
    boolean downloadNextPage(final int preferSize) throws EntryStorageAccessException;
}
