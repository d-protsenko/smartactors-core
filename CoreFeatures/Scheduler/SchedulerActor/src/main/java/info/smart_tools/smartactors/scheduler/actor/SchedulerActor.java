package info.smart_tools.smartactors.scheduler.actor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.scheduler.actor.wrappers.*;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntryStorage;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.EntryScheduleException;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.EntryStorageAccessException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;

import java.util.stream.Collectors;

/**
 * Actor that manages schedules.
 */
public class SchedulerActor {
    private final ISchedulerEntryStorage storage;

    private final IQueue<ITask> taskQueue;

    /**
     * Task downloading entries from remote storage.
     */
    private class DownloadEntriesTask implements ITask {

        @Override
        public void execute() throws TaskExecutionException {
            try {
                if (!storage.downloadNextPage(0)) {
                    taskQueue.put(DownloadEntriesTask.this);
                }
            } catch (EntryStorageAccessException e) {
                throw new TaskExecutionException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * The constructor.
     *
     * @param args    constructor arguments
     * @throws ResolutionException if fails to resolve any dependencies
     * @throws ReadValueException if fails to read any value from arguments object
     * @throws EntryStorageAccessException if fails to download entries saved in database
     * @throws InvalidArgumentException if it occurs
     * @throws InterruptedException if thread is interrupted while enqueuing task downloading entries from remote storage
     */
    public SchedulerActor(final IObject args)
            throws ResolutionException, ReadValueException, EntryStorageAccessException, InvalidArgumentException, InterruptedException {
        String connectionOptionsDependency = (String) args.getValue(
                IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "connectionOptionsDependency"));
        String connectionPoolDependency = (String) args.getValue(
                IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "connectionPoolDependency"));
        String collectionName = (String) args.getValue(
                IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "collectionName"));

        taskQueue = IOC.resolve(Keys.getOrAdd("task_queue"));

        Object connectionOptions = IOC.resolve(Keys.getOrAdd(connectionOptionsDependency));
        IPool connectionPool = IOC.resolve(Keys.getOrAdd(connectionPoolDependency), connectionOptions);
        storage = IOC.resolve(Keys.getOrAdd(ISchedulerEntryStorage.class.getCanonicalName()),
                connectionPool,
                collectionName);

        taskQueue.put(new DownloadEntriesTask());
    }

    /**
     * Create new entry.
     *
     * @param message    the message
     * @throws ResolutionException if error occurs resolving new entry
     * @throws ReadValueException if error occurs reading value from message
     */
    public void addEntry(final AddEntryQueryMessage message)
            throws ResolutionException, ReadValueException {
        IOC.resolve(Keys.getOrAdd("new scheduler entry"), message.getEntryArguments(), storage);
    }

    /**
     * Create new entry.
     *
     * @param message    the message
     * @throws ResolutionException if error occurs resolving new entry
     * @throws ReadValueException if error occurs reading value from message
     * @throws ChangeValueException if error occurs setting value from message
     */
    public void addEntryWithSettingId(final SetEntryIdMessage message)
            throws ResolutionException, ReadValueException, ChangeValueException {
        ISchedulerEntry entry = IOC.resolve(Keys.getOrAdd("new scheduler entry"), message.getEntryArguments(), storage);
        message.setEntryId(entry.getId());
    }

    /**
     * Create list of new entries.
     *
     * @param message    the message
     * @throws ResolutionException if error occurs resolving new entry
     * @throws ReadValueException if error occurs reading value from message
     */
    public void addEntryList(final AddEntryQueryListMessage message)
            throws ResolutionException, ReadValueException {
        for (IObject entry : message.getEntryArgumentsList()) {
            IOC.resolve(Keys.getOrAdd("new scheduler entry"), entry, storage);
        }
    }

    /**
     * List all locally saved entries.
     *
     * @param message    the query message
     * @throws ChangeValueException if error occurs writing the result
     * @throws EntryStorageAccessException if error occurs accessing entry storage
     */
    public void listEntries(final ListEntriesQueryMessage message)
            throws ChangeValueException, EntryStorageAccessException {
        message.setEntries(
                storage.listLocalEntries().stream()
                        .map(ISchedulerEntry::getState)
                        .collect(Collectors.toList()));
    }

    /**
     * Delete and cancel a scheduler entry.
     *
     * @param message    the query message
     * @throws ReadValueException if error occurs reading value from the message
     * @throws EntryStorageAccessException if error occurs accessing entry storage to get or delete the entry
     * @throws EntryScheduleException if error occurs cancelling the entry
     */
    public void deleteEntry(final DeleteEntryQueryMessage message)
            throws ReadValueException, EntryStorageAccessException, EntryScheduleException {
        storage.getEntry(message.getEntryId()).cancel();
    }
}
