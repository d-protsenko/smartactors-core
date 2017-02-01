package info.smart_tools.smartactors.scheduler.actor.impl;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.scheduler.actor.impl.remote_storage.IRemoteEntryStorage;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntryStorage;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntryStorageObserver;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.EntryScheduleException;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.EntryStorageAccessException;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.SchedulerEntryStorageObserverException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
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

    private final List<ISchedulerEntry> refreshList;

    private final Object localStorageLock;

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

        refreshList = new ArrayList<>();

        localStorageLock = new Object();
    }

    @Override
    public void save(final ISchedulerEntry entry)
            throws EntryStorageAccessException {
        remoteEntryStorage.saveEntry(entry);
    }

    @Override
    public void notifyActive(final ISchedulerEntry entry)
            throws EntryStorageAccessException {
        synchronized (localStorageLock) {
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

        remoteEntryStorage.weakSaveEntry(entry);
    }

    @Override
    public void notifyInactive(final ISchedulerEntry entry, final boolean keepReference) throws EntryStorageAccessException {
        synchronized (localStorageLock) {
            activeEntries.remove(entry.getId(), entry);

            if (keepReference) {
                strongSuspendEntries.put(entry.getId(), entry);
            } else {
                weakSuspendEntries.put(entry.getId(), new WeakReference<>(entry));
            }
        }
    }

    @Override
    public void delete(final ISchedulerEntry entry)
            throws EntryStorageAccessException {
        synchronized (localStorageLock) {
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
    }

    @Override
    public List<ISchedulerEntry> listLocalEntries()
            throws EntryStorageAccessException {
        synchronized (localStorageLock) {
            List<ISchedulerEntry> localEntries = new ArrayList<>(activeEntries.size() + strongSuspendEntries.size());
            localEntries.addAll(activeEntries.values());
            localEntries.addAll(strongSuspendEntries.values());
            // TODO:: Enumerate remote entries (?)
            return localEntries;
        }
    }

    @Override
    public ISchedulerEntry getEntry(final String id)
            throws EntryStorageAccessException {
        ISchedulerEntry localEntry = getLocalEntry(id);

        if (null != localEntry) {
            return localEntry;
        }

        IObject savedEntryState = remoteEntryStorage.querySingleEntry(id);

        try {
            return IOC.resolve(Keys.getOrAdd("restore scheduler entry"), savedEntryState, this);
        } catch (ResolutionException e) {
            throw new EntryStorageAccessException("Error occurred restoring required entry from state saved in remote storage.");
        }
    }

    /**
     * Suspend active entries that are scheduled on too late time and awake suspended entries scheduled for not-so late time.
     *
     * <pre>
     *     | . . . . . . . . . . . . . . | . . . . . . . . . . . . . . | . . . . . . . . . (time) >
     *     |now                          |awakeUntil                   |suspendAfter
     *     | (awake everything here)     | (do nothing)                | (suspend everything here)
     * </pre>
     *
     * @param awakeUntil      the time entries scheduled until should be awaken
     * @param suspendAfter    the time entries scheduled after should be suspended
     * @throws EntryStorageAccessException if error occurs accessing entry storage while awakening/suspending some entry
     * @throws EntryScheduleException if error occurs rescheduling some entry to awake it
     */
    public void refresh(final long awakeUntil, final long suspendAfter)
            throws EntryStorageAccessException, EntryScheduleException {
        synchronized (localStorageLock) {
            // Keep references in separate list to avoid ConcurrentModificationException (awake/suspend methods may remove the entry from the
            // map we are iterating over)
            refreshList.clear();
            for (ISchedulerEntry suspendedEntry : strongSuspendEntries.values()) {
                if (suspendedEntry.getLastTime() < awakeUntil) {
                    refreshList.add(suspendedEntry);
                }
            }

            for (ISchedulerEntry entry : refreshList) {
                entry.awake();
            }

            refreshList.clear();
            for (ISchedulerEntry activeEntry : activeEntries.values()) {
                if (activeEntry.getLastTime() > suspendAfter) {
                    refreshList.add(activeEntry);
                }
            }

            for (ISchedulerEntry entry : refreshList) {
                entry.suspend();
            }
        }
    }

    /**
     * Get entry saved locally (active or suspended).
     *
     * @param id    identifier of the entry
     * @return the entry or {@code null} if there is no entry with given identifier in local storage
     */
    public ISchedulerEntry getLocalEntry(final String id) {
        synchronized (localStorageLock) {
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

            return null;
        }
    }
}
