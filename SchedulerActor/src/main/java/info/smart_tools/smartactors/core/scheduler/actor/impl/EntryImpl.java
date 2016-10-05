package info.smart_tools.smartactors.core.scheduler.actor.impl;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.message_bus.MessageBus;
import info.smart_tools.smartactors.core.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.core.scheduler.interfaces.ISchedulerEntryStorage;
import info.smart_tools.smartactors.core.scheduler.interfaces.ISchedulingStrategy;
import info.smart_tools.smartactors.core.scheduler.interfaces.exceptions.EntryScheduleException;
import info.smart_tools.smartactors.core.scheduler.interfaces.exceptions.EntryStorageAccessException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.SerializeException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.timer.interfaces.itimer.ITimer;
import info.smart_tools.smartactors.timer.interfaces.itimer.ITimerTask;
import info.smart_tools.smartactors.timer.interfaces.itimer.exceptions.TaskScheduleException;

import java.util.UUID;

/**
 * Implementation of {@link ISchedulerEntry}.
 */
public final class EntryImpl implements ISchedulerEntry {
    private final ISchedulerEntryStorage storage;
    private final IObject state;
    private final String id;
    private final ISchedulingStrategy strategy;
    private long lastScheduledTime;
    private ITimerTask timerTask;
    private final ITimer timer;
    private boolean isCancelled;
    private final ITask task = this::executeTask;

    private final IFieldName idFieldName;
    private final IFieldName messageFieldName;

    /**
     * The constructor.
     *
     * @param state       entry state object
     * @param strategy    scheduling strategy
     * @param storage     storage to save entry in
     * @throws ResolutionException if fails to resolve any dependencies
     * @throws ReadValueException if error occurs reading values from state object
     * @throws InvalidArgumentException if error occurs reading values from state object
     */
    public EntryImpl(
            final IObject state,
            final ISchedulingStrategy strategy,
            final ISchedulerEntryStorage storage)
                throws ResolutionException, ReadValueException, InvalidArgumentException {
        idFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "entryId");
        messageFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "message");

        this.timer = IOC.resolve(Keys.getOrAdd("timer"));

        this.storage = storage;
        this.state = state;
        this.strategy = strategy;
        this.lastScheduledTime = -1;

        this.timerTask = null;
        this.isCancelled = false;

        this.id = (String) state.getValue(idFieldName);
    }

    /**
     * Create a new entry from given description.
     *
     * @param args       entry description
     * @param storage    the {@link EntryStorage storage} to store entry in (if necessary)
     *
     * @return the new entry
     *
     * @throws ResolutionException if cannot resolve dependencies
     * @throws ReadValueException if cannot read description fields
     * @throws ChangeValueException if can not modify description to use it as entry state object
     * @throws InvalidArgumentException if invalid arguments were passed to some method
     */
    public static EntryImpl newEntry(final IObject args, final ISchedulerEntryStorage storage)
            throws ResolutionException, ReadValueException, ChangeValueException, InvalidArgumentException {
        IFieldName idFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "entryId");
        IFieldName strategyFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "strategy");

        ISchedulingStrategy strategy = IOC.resolve(Keys.getOrAdd((String) args.getValue(strategyFieldName)));

        String id = UUID.randomUUID().toString();

        args.setValue(idFieldName, id);

        EntryImpl entry = new EntryImpl(
                args,
                strategy,
                storage);

        strategy.init(entry, args);
        return entry;
    }

    /**
     * Create new entry from entry state object loaded from database.
     *
     * @param savedState    the saved entry state object
     * @param storage       the storage to use to store the entry (or to delete it from when it is completed)
     *
     * @return the new entry
     *
     * @throws ResolutionException if cannot resolve dependencies
     * @throws ReadValueException if cannot read description fields
     * @throws InvalidArgumentException if invalid arguments were passed to some method
     */
    public static EntryImpl restoreEntry(final IObject savedState, final ISchedulerEntryStorage storage)
            throws ResolutionException, ReadValueException, InvalidArgumentException {
        IFieldName strategyFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "strategy");

        ISchedulingStrategy strategy = IOC.resolve(Keys.getOrAdd((String) savedState.getValue(strategyFieldName)));

        EntryImpl entry = new EntryImpl(
                savedState,
                strategy,
                storage);

        strategy.restore(entry);
        return entry;
    }

    private void executeTask() throws TaskExecutionException {
        try {
            MessageBus.send(makeMessageClone());
            strategy.postProcess(this);
        } catch (Exception e) {
            strategy.processException(this, e);
        }
    }

    @Override
    public IObject getState() {
        return state;
    }

    @Override
    public void save() throws EntryStorageAccessException {
        storage.save(this);
    }

    @Override
    public void cancel() throws EntryStorageAccessException {
        if (!isCancelled) {
            this.isCancelled = true;
            if (this.timerTask != null) {
                this.timerTask.cancel();
            }

            storage.delete(this);
        }
    }

    @Override
    public void scheduleNext(final long time)
            throws EntryScheduleException {
        try {
            if (this.timerTask == null) {
                this.timerTask = timer.schedule(task, time);
            } else {
                timerTask.reschedule(time);
            }

            lastScheduledTime = time;

            storage.saveLocally(this);
        } catch (TaskScheduleException | EntryStorageAccessException e) {
            throw new EntryScheduleException("Could not reschedule entry.", e);
        }
    }

    @Override
    public long getLastTime() {
        return lastScheduledTime;
    }

    @Override
    public String getId() {
        return id;
    }

    private IObject makeMessageClone()
            throws ResolutionException, ReadValueException, InvalidArgumentException, SerializeException {
        IObject originalMessage = (IObject) state.getValue(messageFieldName);
        String serialized = originalMessage.serialize();
        return IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()), serialized);
    }
}
