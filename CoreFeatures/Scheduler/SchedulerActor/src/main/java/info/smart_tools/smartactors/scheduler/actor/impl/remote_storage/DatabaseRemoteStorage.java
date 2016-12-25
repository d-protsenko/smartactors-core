package info.smart_tools.smartactors.scheduler.actor.impl.remote_storage;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.base.pool_guard.IPoolGuard;
import info.smart_tools.smartactors.base.pool_guard.PoolGuard;
import info.smart_tools.smartactors.base.pool_guard.exception.PoolGuardException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntryStorage;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.EntryStorageAccessException;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;

/**
 * Remote entry storage storing entries in database.
 */
public class DatabaseRemoteStorage implements IRemoteEntryStorage {
    private static final int DEFAULT_PAGE_SIZE = 100;

    private final IPool connectionPool;
    private final String collectionName;

    // Size of download page
    private int downloadPageSize = DEFAULT_PAGE_SIZE;
    //
    private Object lastDownloadedId = null;
    // True if all entries are downloaded from remote storage
    private boolean isInitialized = false;

    private final IFieldName filterFieldName;
    private final IFieldName gtFieldName;
    private final IFieldName entryIdFieldName;

    /**
     * The constructor.
     *
     * @param connectionPool    database connection pool
     * @param collectionName    name of database collection to use
     * @throws ResolutionException if error occurs resolving any dependencies
     */
    public DatabaseRemoteStorage(final IPool connectionPool, final String collectionName)
            throws ResolutionException {
        this.connectionPool = connectionPool;
        this.collectionName = collectionName;

        filterFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "filter");
        gtFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "$gt");
        entryIdFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "entryId");
    }

    @Override
    public void saveEntry(final ISchedulerEntry entry) throws EntryStorageAccessException {
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
    public void deleteEntry(final ISchedulerEntry entry) throws EntryStorageAccessException {
        try (IPoolGuard guard = new PoolGuard(connectionPool)) {

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

    @Override
    public boolean downloadNextPage(final int preferSize, final ISchedulerEntryStorage localStorage) throws EntryStorageAccessException {
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
                                IOC.resolve(Keys.getOrAdd("restore scheduler entry"), obj, localStorage);
                            }

                            if (docs.length < downloadPageSize) {
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
