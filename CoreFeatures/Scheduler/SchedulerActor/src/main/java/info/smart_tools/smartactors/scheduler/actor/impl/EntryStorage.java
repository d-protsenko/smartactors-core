package info.smart_tools.smartactors.scheduler.actor.impl;

import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.scheduler.actor.impl.remote_storage.IRemoteEntryStorage;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntryStorage;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntryStorageObserver;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.EntryScheduleException;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.EntryStorageAccessException;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.SchedulerEntryStorageObserverException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link ISchedulerEntryStorage}.
 */
public class EntryStorage implements ISchedulerEntryStorage {

    private final ConcurrentHashMap<String, ISchedulerEntry> localEntries = new ConcurrentHashMap<>();

    private final IRemoteEntryStorage remoteEntryStorage;
    private final ISchedulerEntryStorageObserver observer;

    private boolean isInitialDownloadComplete;

    /**
     * The constructor.
     *
     * @param remoteEntryStorage remote storage to use
     * @throws ResolutionException if fails to resolve any dependencies
     */
    public EntryStorage(final IRemoteEntryStorage remoteEntryStorage)
            throws ResolutionException {
        this(remoteEntryStorage, null);
    }

    /**
     * The constructor.
     *
     * @param remoteEntryStorage    remote storage to use
     * @param observer              the observer that should be notified on events occurring within this storage
     * @throws ResolutionException if fails to resolve any dependencies
     */
    public EntryStorage(final IRemoteEntryStorage remoteEntryStorage, final ISchedulerEntryStorageObserver observer)
            throws ResolutionException {
        this.remoteEntryStorage = remoteEntryStorage;
        this.observer = (observer == null) ? NullEntryStorageObserver.INSTANCE : observer;
        this.isInitialDownloadComplete = false;
    }

    @Override
    public void save(final ISchedulerEntry entry)
            throws EntryStorageAccessException {
        remoteEntryStorage.saveEntry(entry);
    }

    @Override
    public void saveLocally(final ISchedulerEntry entry)
            throws EntryStorageAccessException {
        ISchedulerEntry oldEntry = localEntries.put(entry.getId(), entry);

        try {
            observer.onUpdateEntry(entry);
        } catch (SchedulerEntryStorageObserverException e) {
            throw new EntryStorageAccessException("Error occurred notifying observer on updated entry.", e);
        }

        if (null != oldEntry && entry != oldEntry) {
            try {
                oldEntry.cancel();
            } catch (EntryScheduleException e) {
                throw new EntryStorageAccessException("Error cancelling duplicate entry.", e);
            }
        }
    }

    @Override
    public void delete(final ISchedulerEntry entry)
            throws EntryStorageAccessException {
        try {
            localEntries.remove(entry.getId(), entry);
            remoteEntryStorage.deleteEntry(entry);
            observer.onCancelEntry(entry);
        } catch (SchedulerEntryStorageObserverException e) {
            throw new EntryStorageAccessException("Error occurred notifying observer on deleted entry.");
        }

    }

    @Override
    public List<ISchedulerEntry> listLocalEntries()
            throws EntryStorageAccessException {
        return new ArrayList<>(localEntries.values());
    }

    @Override
    public ISchedulerEntry getEntry(final String id)
            throws EntryStorageAccessException {
        ISchedulerEntry entry = localEntries.get(id);

        if (null == entry) {
            throw new EntryStorageAccessException(MessageFormat.format("Cannot find entry with id=''{0}''.", id));
        }

        return entry;
    }

    @Override
    public boolean downloadNextPage(final int preferSize)
            throws EntryStorageAccessException {
        boolean complete = remoteEntryStorage.downloadNextPage(preferSize, this);

        if (complete && !isInitialDownloadComplete) {
            isInitialDownloadComplete = true;
            try {
                observer.onDownloadComplete();
            } catch (SchedulerEntryStorageObserverException e) {
                throw new EntryStorageAccessException("Error occurred notifying storage observer on download completion.", e);
            }
        }

        return complete;
    }
}
