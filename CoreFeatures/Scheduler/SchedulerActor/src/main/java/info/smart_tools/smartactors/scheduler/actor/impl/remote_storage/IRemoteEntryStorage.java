package info.smart_tools.smartactors.scheduler.actor.impl.remote_storage;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntryStorage;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.EntryStorageAccessException;

import java.util.List;

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
     * Search for a record of entry with given id.
     *
     * @param id    identifier of the entry
     * @return entry state saved in remote storage or {@code null} if there is mo such record
     * @throws EntryStorageAccessException if error occurs
     */
    IObject querySingleEntry(String id) throws EntryStorageAccessException;

    /**
     * Download saved states of entries.
     *
     * @param untilTime    the time entries scheduled until should be downloaded
     * @param lastSkip     the last object downloaded using this method with the same {@code untilTime} or {@code null} if this is the first
     *                     call
     * @return list of found entries
     * @throws EntryStorageAccessException if error occurs
     */
    List<IObject> downloadEntries(long untilTime, IObject lastSkip) throws EntryStorageAccessException;

    /**
     * Update record of the given entry if it is exist and it is necessary. This method should be called after the entry is updated.
     *
     * @param entry    the entry to save
     * @throws EntryStorageAccessException if error occurs
     */
    void weakSaveEntry(ISchedulerEntry entry) throws EntryStorageAccessException;
}
