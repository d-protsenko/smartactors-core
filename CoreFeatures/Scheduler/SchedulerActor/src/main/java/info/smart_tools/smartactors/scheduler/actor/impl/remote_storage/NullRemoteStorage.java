package info.smart_tools.smartactors.scheduler.actor.impl.remote_storage;

import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntryStorage;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.EntryStorageAccessException;

/**
 * Implementation of {@link IRemoteEntryStorage remote entry storage} that does not actually store entries.
 */
public final class NullRemoteStorage implements IRemoteEntryStorage {
    private NullRemoteStorage() {
    }

    @Override
    public void saveEntry(final ISchedulerEntry entry) throws EntryStorageAccessException {
        throw new EntryStorageAccessException("Entry saving not supported by this entry storage.");
    }

    @Override
    public void deleteEntry(final ISchedulerEntry entry) throws EntryStorageAccessException {
        //do nothing
    }

    @Override
    public boolean downloadNextPage(final int preferredSize, final ISchedulerEntryStorage localStorage) throws EntryStorageAccessException {
        return true;
    }

    /**
     * The single instance.
     */
    public static final NullRemoteStorage INSTANCE = new NullRemoteStorage();
}
