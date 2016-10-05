package info.smart_tools.smartactors.core.scheduling_strategies;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.core.scheduler.interfaces.ISchedulingStrategy;
import info.smart_tools.smartactors.core.scheduler.interfaces.exceptions.EntryScheduleException;
import info.smart_tools.smartactors.core.scheduler.interfaces.exceptions.EntryStorageAccessException;
import info.smart_tools.smartactors.core.scheduler.interfaces.exceptions.SchedulingStrategyExecutionException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Scheduling strategy that schedules an entry once in a fixed time.
 *
 * <p>
 * Expects the following arguments:
 * </p>
 *
 * <ul>
 *     <li>{@code "time"} - the time (ISO-8601, without timezone) when to schedule entry</li>
 *     <li>{@code "save"} - {@code true} if the entry should be saved to database and {@code false} if not</li>
 *     <li>{@code "neverTooLate"} - {@code true} if the entry should be scheduled at current time when restored from database with {@code
 *     "time"} before current time, {@code false} otherwise. This argument may be omitted if {@code "save"} is {@code false}.</li>
 * </ul>
 */
public class OnceSchedulingStrategy implements ISchedulingStrategy {
    private final IFieldName timeFieldName;
    private final IFieldName saveFieldName;
    private final IFieldName neverTooLateFieldName;

    private long datetimeToMillis(final LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    /**
     * The constructor.
     *
     * @throws ResolutionException if fails to resolve any dependency
     */
    public OnceSchedulingStrategy()
            throws ResolutionException {
        timeFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "time");
        saveFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "save");
        neverTooLateFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "neverTooLate");
    }

    @Override
    public void init(final ISchedulerEntry entry, final IObject args) throws SchedulingStrategyExecutionException {
        try {
            LocalDateTime time = LocalDateTime.parse((String) args.getValue(timeFieldName));
            Boolean save = (Boolean) args.getValue(saveFieldName);

            entry.getState().setValue(timeFieldName, time.toString());

            entry.scheduleNext(datetimeToMillis(time));

            if (save) {
                Boolean ntl = (Boolean) args.getValue(neverTooLateFieldName);

                entry.getState().setValue(neverTooLateFieldName, ntl);
                entry.save();
            }
        } catch (ReadValueException | InvalidArgumentException | EntryStorageAccessException | ChangeValueException
                | EntryScheduleException e) {
            throw new SchedulingStrategyExecutionException("Error occurred initializing entry.", e);
        }
    }

    @Override
    public void postProcess(final ISchedulerEntry entry) throws SchedulingStrategyExecutionException {
        try {
            entry.cancel();
        } catch (EntryStorageAccessException | EntryScheduleException e) {
            throw new SchedulingStrategyExecutionException("Error occurred cancelling completed entry.", e);
        }
    }

    @Override
    public void restore(final ISchedulerEntry entry) throws SchedulingStrategyExecutionException {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime time = LocalDateTime.parse((String) entry.getState().getValue(timeFieldName));
            Boolean ntl = (Boolean) entry.getState().getValue(neverTooLateFieldName);

            if (now.isAfter(time) && !ntl) {
                entry.cancel();
            } else {
                entry.scheduleNext(datetimeToMillis(time));
            }
        } catch (ReadValueException | InvalidArgumentException | EntryScheduleException | EntryStorageAccessException e) {
            throw new SchedulingStrategyExecutionException("Error occurred restoring an entry.", e);
        }
    }

    @Override
    public void processException(final ISchedulerEntry entry, final Throwable e) throws SchedulingStrategyExecutionException {
        try {
            entry.cancel();
        } catch (EntryStorageAccessException | EntryScheduleException ee) {
            throw new SchedulingStrategyExecutionException("Error occurred cancelling failed entry.", ee);
        }
    }
}
