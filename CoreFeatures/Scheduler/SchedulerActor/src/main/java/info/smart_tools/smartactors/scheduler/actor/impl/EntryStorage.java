package info.smart_tools.smartactors.scheduler.actor.impl;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.base.pool_guard.IPoolGuard;
import info.smart_tools.smartactors.base.pool_guard.PoolGuard;
import info.smart_tools.smartactors.base.pool_guard.exception.PoolGuardException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntryStorage;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntryStorageObserver;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.EntryScheduleException;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.EntryStorageAccessException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.SchedulerEntryStorageObserverException;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link ISchedulerEntryStorage}.
 */
public class EntryStorage implements ISchedulerEntryStorage {
    private static final int DEFAULT_PAGE_SIZE = 100;

    private final ConcurrentHashMap<String, ISchedulerEntry> localEntries = new ConcurrentHashMap<>();

    private final IPool connectionPool;
    private final String collectionName;

    private final IFieldName filterFieldName;
    private final IFieldName gtFieldName;
    private final IFieldName entryIdFieldName;

    // Size of download page
    private int downloadPageSize = DEFAULT_PAGE_SIZE;
    //
    private Object lastDownloadedId = null;
    // True if all entries are downloaded from remote storage
    private boolean isInitialized = false;

    private ISchedulerEntryStorageObserver observer;

    /**
     * The constructor.
     *
     * @param connectionPool    database connection pool
     * @param collectionName    name of database collection to use
     * @throws ResolutionException if fails to resolve any dependencies
     */
    public EntryStorage(final IPool connectionPool, final String collectionName)
            throws ResolutionException {
        this(connectionPool, collectionName, null);
    }

    /**
     * The constructor.
     *
     * @param connectionPool    database connection pool
     * @param collectionName    name of database collection to use
     * @param observer          the observer that should be notified on events occurring within this storage
     * @throws ResolutionException if fails to resolve any dependencies
     */
    public EntryStorage(final IPool connectionPool, final String collectionName, final ISchedulerEntryStorageObserver observer)
            throws ResolutionException {
        this.connectionPool = connectionPool;
        this.collectionName = collectionName;
        this.observer = (observer == null) ? NullEntryStorageObserver.INSTANCE : observer;

        filterFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "filter");
        gtFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "$gt");
        entryIdFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "entryId");
    }

    @Override
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

            observer.onCancelEntry(entry);
        } catch (PoolGuardException | ResolutionException | TaskExecutionException e) {
            throw new EntryStorageAccessException("Error occurred deleting entry from database.", e);
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
        if (isInitialized) {
            return true;
        }

        if (preferSize > 0 && lastDownloadedId == null) {
            downloadPageSize = preferSize;
        }

        try (IPoolGuard guard = new PoolGuard(connectionPool)) {
            Object connection = guard.getObject();

            IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                    String.format("{'filter':{},'page':{'size':%s,'number':%s},'sort':[{'entryId':'asc'}]}"
                            .replace('\'', '"'), downloadPageSize, 1));

            if (lastDownloadedId != null) {
                IObject entryIdFilter = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
                entryIdFilter.setValue(gtFieldName, lastDownloadedId);
                ((IObject) query.getValue(filterFieldName)).setValue(entryIdFieldName, entryIdFilter);
            }

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
                                observer.onDownloadComplete();
                                isInitialized = true;
                            } else {
                                lastDownloadedId = docs[docs.length - 1].getValue(entryIdFieldName);
                            }
                        } catch (Exception e) {
                            throw new ActionExecuteException(e);
                        }
                    }
            );

            task.execute();
        } catch (PoolGuardException | ResolutionException | TaskExecutionException | ReadValueException | ChangeValueException
                | InvalidArgumentException e) {
            throw new EntryStorageAccessException("Error occurred downloading page of scheduler entries.", e);
        }

        return isInitialized;
    }
}
