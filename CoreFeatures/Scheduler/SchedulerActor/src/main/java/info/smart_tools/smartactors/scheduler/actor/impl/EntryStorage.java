package info.smart_tools.smartactors.scheduler.actor.impl;

import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.scheduler.actor.impl.remote_storage.IRemoteEntryStorage;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntryStorage;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntryStorageObserver;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.EntryScheduleException;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.EntryStorageAccessException;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.SchedulerEntryStorageObserverException;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Implementation of {@link ISchedulerEntryStorage}.
 */
public class EntryStorage implements ISchedulerEntryStorage {
    private final IRemoteEntryStorage remoteEntryStorage;
    private final ISchedulerEntryStorageObserver observer;

    private final Map<String, ISchedulerEntry> activeEntries;
    private final Map<String, ISchedulerEntry> strongSuspendEntries;
    private final Map<String, WeakReference<ISchedulerEntry>> weakSuspendEntries;

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

        activeEntries = new HashMap<>();
        strongSuspendEntries = new HashMap<>();
        weakSuspendEntries = new WeakHashMap<>();
    }

    @Override
    public void save(final ISchedulerEntry entry)
            throws EntryStorageAccessException {
        remoteEntryStorage.saveEntry(entry);
    }

    @Override
    public void notifyActive(final ISchedulerEntry entry)
            throws EntryStorageAccessException {
        //TODO:: Lock (?)
        ISchedulerEntry oldEntry = activeEntries.put(entry.getId(), entry);

        strongSuspendEntries.remove(entry.getId(), entry);
        weakSuspendEntries.remove(entry.getId(), entry);

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
    public void notifyInactive(final ISchedulerEntry entry, final boolean keepReference) throws EntryStorageAccessException {
        //TODO:: Lock (?)
        activeEntries.remove(entry.getId(), entry);

        if (keepReference) {
            strongSuspendEntries.put(entry.getId(), entry);
        } else {
            weakSuspendEntries.put(entry.getId(), new WeakReference<>(entry));
        }
    }

    @Override
    public void delete(final ISchedulerEntry entry)
            throws EntryStorageAccessException {
        //TODO:: Lock (?)
        try {
            activeEntries.remove(entry.getId(), entry);
            strongSuspendEntries.remove(entry.getId(), entry);
            weakSuspendEntries.remove(entry.getId(), entry);

            remoteEntryStorage.deleteEntry(entry);
            observer.onCancelEntry(entry);
        } catch (SchedulerEntryStorageObserverException e) {
            throw new EntryStorageAccessException("Error occurred notifying observer on deleted entry.");
        }
    }

    @Override
    public List<ISchedulerEntry> listLocalEntries()
            throws EntryStorageAccessException {
        // TODO::
        return null;
    }

    @Override
    public ISchedulerEntry getEntry(final String id)
            throws EntryStorageAccessException {
        //TODO:: Lock (?)

        if (weakSuspendEntries.containsKey(id)) {
            ISchedulerEntry e = weakSuspendEntries.get(id).get();

            if (null == e) {
                weakSuspendEntries.remove(id);
            } else {
                return e;
            }
        }

        if (activeEntries.containsKey(id)) {
            return activeEntries.get(id);
        }

        if (strongSuspendEntries.containsKey(id)) {
            return strongSuspendEntries.get(id);
        }

        //TODO:: Query from DB
        return null;
    }

    @Override
    public boolean downloadNextPage(final int preferSize)
            throws EntryStorageAccessException {
        boolean complete = remoteEntryStorage.downloadNextPage(preferSize, this);

        return complete;
    }
}
