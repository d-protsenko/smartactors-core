package info.smart_tools.smartactors.scheduler.actor.impl.remote_storage;

import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntryStorage;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.EntryStorageAccessException;

/**
 * Interface for remote storage of {@link ISchedulerEntry scheduler entries}.
 */
public interface IRemoteEntryStorage {
    /**
     * Save entry in remote storage.
     *
     * @param entry    the entry to save
     * @throws EntryStorageAccessException if error occurs
     */
    void saveEntry(ISchedulerEntry entry) throws EntryStorageAccessException;

    /**
     * Delete the entry.
     *
     * @param entry    the entry to delete
     * @throws EntryStorageAccessException if error occurs
     */
    void deleteEntry(ISchedulerEntry entry) throws EntryStorageAccessException;

    /**
     * Download next page of entries
     *
     * @param preferredSize    preferred size of page
     * @param localStorage     local storage to store restored entries in
     * @return {@code true} if all entries are downloaded and no mo re invocations of this method required
     * @throws EntryStorageAccessException if error occurs
     */
    boolean downloadNextPage(int preferredSize, ISchedulerEntryStorage localStorage) throws EntryStorageAccessException;
}
