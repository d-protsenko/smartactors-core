package info.smart_tools.smartactors.core.scheduler.actor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.itimer.ITimer;
import info.smart_tools.smartactors.core.itimer.ITimerTask;
import info.smart_tools.smartactors.core.itimer.exceptions.TaskScheduleException;
import info.smart_tools.smartactors.core.message_bus.MessageBus;
import info.smart_tools.smartactors.core.scheduler.interfaces.ISchedulerEntry;
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

import java.util.UUID;

/**
 * Implementation of {@link ISchedulerEntry}.
 */
final class EntryImpl implements ISchedulerEntry {
    private final IObject state;
    private final String id;
    private boolean saved;
    private final ISchedulingStrategy strategy;
    private long lastScheduledTime;
    private ITimerTask timerTask;
    private final ITimer timer;
    private boolean isCancelled;
    private final ITask task = this::executeTask;

    private final IFieldName idFieldName;
    private final IFieldName messageFieldName;

    private EntryImpl(
            final IObject state,
            final boolean saved,
            final ISchedulingStrategy strategy)
                throws ResolutionException, ReadValueException, InvalidArgumentException {
        idFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "entryId");
        messageFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "message");

        this.timer = IOC.resolve(Keys.getOrAdd("timer"));

        this.state = state;
        this.saved = saved;
        this.strategy = strategy;
        this.lastScheduledTime = -1;

        this.timerTask = null;
        this.isCancelled = false;

        this.id = (String) state.getValue(idFieldName);
    }

    public static EntryImpl newEntry(final IObject args)
            throws ResolutionException, ReadValueException, ChangeValueException, InvalidArgumentException {
        IFieldName idFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "entryId");
        IFieldName strategyFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "strategy");

        ISchedulingStrategy strategy = IOC.resolve(Keys.getOrAdd((String) args.getValue(strategyFieldName)));

        String id = UUID.randomUUID().toString();

        args.setValue(idFieldName, id);

        EntryImpl entry = new EntryImpl(
                args,
                false,
                strategy);

        strategy.init(entry, args);
        return entry;
    }

    public static EntryImpl restoreEntry(final IObject savedState)
            throws ResolutionException, ReadValueException, InvalidArgumentException {
        IFieldName strategyFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "strategy");

        ISchedulingStrategy strategy = IOC.resolve(Keys.getOrAdd((String) savedState.getValue(strategyFieldName)));

        EntryImpl entry = new EntryImpl(
                savedState,
                true,
                strategy);

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
        this.saved = true;
        //TODO: Save
    }

    @Override
    public void cancel() throws EntryStorageAccessException {
        if (!isCancelled) {
            this.isCancelled = true;
            if (this.timerTask != null) {
                this.timerTask.cancel();
            }

            //if (saved) {
                //TODO: Delete if saved
            //}
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
        } catch (TaskScheduleException e) {
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
