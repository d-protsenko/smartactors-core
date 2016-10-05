package info.smart_tools.smartactors.core.scheduler.actor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.core.scheduler.actor.wrappers.AddEntryQueryMessage;
import info.smart_tools.smartactors.core.scheduler.actor.wrappers.DeleteEntryQueryMessage;
import info.smart_tools.smartactors.core.scheduler.actor.wrappers.ListEntriesQueryMessage;
import info.smart_tools.smartactors.core.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.core.scheduler.interfaces.ISchedulerEntryStorage;
import info.smart_tools.smartactors.core.scheduler.interfaces.exceptions.EntryScheduleException;
import info.smart_tools.smartactors.core.scheduler.interfaces.exceptions.EntryStorageAccessException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

import java.util.stream.Collectors;

/**
 * Actor that manages schedules.
 */
public class SchedulerActor {
    private final ISchedulerEntryStorage storage;

    /**
     * The constructor.
     *
     * @param args    constructor arguments
     * @throws ResolutionException if fails to resolve any dependencies
     * @throws ReadValueException if fails to read any value from arguments object
     * @throws EntryStorageAccessException if fails to download entries saved in database
     * @throws InvalidArgumentException if it occurs
     */
    public SchedulerActor(final IObject args)
            throws ResolutionException, ReadValueException, EntryStorageAccessException, InvalidArgumentException {
        String connectionOptionsDependency = (String) args.getValue(
                IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "connectionOptionsDependency"));
        String connectionPoolDependency = (String) args.getValue(
                IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "connectionPoolDependency"));
        String collectionName = (String) args.getValue(
                IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "collectionName"));

        Object connectionOptions = IOC.resolve(Keys.getOrAdd(connectionOptionsDependency));
        IPool connectionPool = IOC.resolve(Keys.getOrAdd(connectionPoolDependency), connectionOptions);
        storage = IOC.resolve(Keys.getOrAdd(ISchedulerEntryStorage.class.getCanonicalName()),
                connectionPool,
                collectionName);

        // TODO: Download schedules asynchronously
        while (true) {
            if (storage.downloadNextPage(0)) {
                break;
            }
        }
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
