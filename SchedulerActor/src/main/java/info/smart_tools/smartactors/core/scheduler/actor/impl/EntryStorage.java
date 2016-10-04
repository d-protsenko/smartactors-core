package info.smart_tools.smartactors.core.scheduler.actor.impl;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.base.pool_guard.IPoolGuard;
import info.smart_tools.smartactors.base.pool_guard.PoolGuard;
import info.smart_tools.smartactors.base.pool_guard.exception.PoolGuardException;
import info.smart_tools.smartactors.core.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.core.scheduler.interfaces.exceptions.EntryScheduleException;
import info.smart_tools.smartactors.core.scheduler.interfaces.exceptions.EntryStorageAccessException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides local (in-memory) and remote (database) storage for {@link ISchedulerEntry scheduler entries}.
 */
public class EntryStorage {
    private static final int DEFAULT_PAGE_SIZE = 100;

    private final ConcurrentHashMap<String, ISchedulerEntry> localEntries = new ConcurrentHashMap<>();

    private final IPool connectionPool;
    private final String collectionName;

    // Size of download page
    private int downloadPageSize = DEFAULT_PAGE_SIZE;
    // Number of pages downloaded from remote storage
    private int downloadedPages = 0;
    // True if all entries are downloaded from remote storage
    private boolean isInitialized = false;

    /**
     * The constructor.
     *
     * @param connectionPool    database connection pool
     * @param collectionName    name of database collection to use
     */
    public EntryStorage(final IPool connectionPool, final String collectionName) {
        this.connectionPool = connectionPool;
        this.collectionName = collectionName;
    }

    /**
     * Save the entry to both local and remote storage.
     *
     * @param entry    the entry to save
     * @throws EntryStorageAccessException if error occurs accessing remote storage
     */
    public void save(final ISchedulerEntry entry)
            throws EntryStorageAccessException {
        try (IPoolGuard guard = new PoolGuard(connectionPool)) {
            Object connection = guard.getObject();

            ITask task = IOC.resolve(
                    Keys.getOrAdd("db.collection.upsert"),
                    connection,
                    collectionName,
                    entry.getState());

            task.execute();
        } catch (ResolutionException | PoolGuardException | TaskExecutionException e) {
            throw new EntryStorageAccessException("Error occurred saving scheduler entry to database.", e);
        }
    }

    /**
     * Save the entry to local storage.
     *
     * @param entry    the entry to save
     * @throws EntryStorageAccessException if error occurs
     */
    public void saveLocally(final ISchedulerEntry entry)
            throws EntryStorageAccessException {
        ISchedulerEntry oldEntry = localEntries.put(entry.getId(), entry);

        if (null != oldEntry && entry != oldEntry) {
            try {
                oldEntry.cancel();
            } catch (EntryScheduleException e) {
                throw new EntryStorageAccessException("Error cancelling duplicate entry.", e);
            }
        }
    }

    /**
     * Delete the given entry from both local and remote storage.
     *
     * @param entry    the entry
     * @throws EntryStorageAccessException if error occurs accessing remote storage
     */
    public void delete(final ISchedulerEntry entry)
            throws EntryStorageAccessException {
        try (IPoolGuard guard = new PoolGuard(connectionPool)) {
            if (entry == localEntries.get(entry.getId())) {
                localEntries.remove(entry.getId());
            }

            Object connection = guard.getObject();

            ITask task = IOC.resolve(
                    Keys.getOrAdd("db.collection.delete"),
                    connection,
                    collectionName,
                    entry.getState());

            task.execute();
        } catch (PoolGuardException | ResolutionException | TaskExecutionException e) {
            throw new EntryStorageAccessException("Error occurred deleting entry from database.", e);
        }
    }

    /**
     * Get list of all locally saved entries.
     *
     * @return list of all locally saved entries
     * @throws EntryStorageAccessException if any error occurs
     */
    public List<ISchedulerEntry> listLocalEntries()
            throws EntryStorageAccessException {
        return new ArrayList<>(localEntries.values());
    }

    /**
     * Get (a locally saved) entry with given identifier.
     *
     * @param id    the entry identifier
     * @return the entry saved with given identifier
     * @throws EntryStorageAccessException if there is no entry with given identifier
     */
    public ISchedulerEntry getEntry(final String id)
            throws EntryStorageAccessException {
        ISchedulerEntry entry = localEntries.get(id);

        if (null == entry) {
            throw new EntryStorageAccessException(MessageFormat.format("Cannot find entry with id=''{0}''.", id));
        }

        return entry;
    }

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
    public boolean downloadNextPage(final int preferSize)
            throws EntryStorageAccessException {
        if (isInitialized) {
            return true;
        }

        if (preferSize > 0 && downloadedPages == 0) {
            downloadPageSize = preferSize;
        }

        try (IPoolGuard guard = new PoolGuard(connectionPool)) {
            Object connection = guard.getObject();

            IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                    String.format("{'filter':{},'page':{'size':%s,'number':%s},'sort':[{'entryId':'asc'}]}"
                            .replace('\'', '"'), downloadPageSize, downloadedPages + 1));

            ITask task = IOC.resolve(
                    Keys.getOrAdd("db.collection.search"),
                    connection,
                    collectionName,
                    query,
                    (IAction<IObject[]>) docs -> {
                        try {
                            for (IObject obj : docs) {
                                IOC.resolve(Keys.getOrAdd("restore scheduler entry"), obj, this);
                            }

                            if (docs.length < downloadPageSize) {
                                isInitialized = true;
                            }
                        } catch (Exception e) {
                            throw new ActionExecuteException(e);
                        }
                    }
            );

            ++downloadedPages;

            task.execute();
        } catch (PoolGuardException | ResolutionException | TaskExecutionException e) {
            throw new EntryStorageAccessException("Error occurred downloading page of scheduler entries.", e);
        }

        return isInitialized;
    }
}
