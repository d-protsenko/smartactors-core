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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;


/**
 * {@link ISchedulingStrategy Scheduling strategy} that schedules entry with some fixed interval.
 *
 * <p>
 * Expected configuration arguments are:
 * </p>
 *
 * <ul>
 *     <li>{@code "start"} - date and time (ISO 8601) when the entry should/could be executed first time</li>
 *     <li>{@code "interval"} - interval in ISO-8601 format (e.g. {@code "PT8H10M42.36S"})</li>
 * </ul>
 */
public class ContinuouslyRepeatScheduleStrategy implements ISchedulingStrategy {
    private final IFieldName startFieldName;
    private final IFieldName intervalFieldName;

    private long nextTime(final LocalDateTime startTime, final Duration period, final long now) {
        long lStartTime = datetimeToMillis(startTime);

        if (lStartTime >= now) {
            return lStartTime;
        }

        long lPeriod = period.toMillis();
        long lNextTime = lStartTime + lPeriod * ((now - lStartTime) / lPeriod);

        return (lNextTime >= now) ? lNextTime : (lNextTime + lPeriod);
    }

    private long datetimeToMillis(final LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    /**
     * The constructor.
     *
     * @throws ResolutionException if fails resolving dependencies
     */
    public ContinuouslyRepeatScheduleStrategy()
            throws ResolutionException {
        startFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "start");
        intervalFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "interval");
    }

    @Override
    public void init(final ISchedulerEntry entry, final IObject args) throws SchedulingStrategyExecutionException {
        try {
            String start = (String) args.getValue(startFieldName);
            LocalDateTime startTime;
            Duration interval = Duration.parse((String) args.getValue(intervalFieldName));
            LocalDateTime now = LocalDateTime.now();

            if (start == null) {
                startTime = now;
            } else {
                startTime = LocalDateTime.parse(start);
            }

            entry.getState().setValue(startFieldName, startTime.toString());
            entry.getState().setValue(intervalFieldName, interval.toString());

            entry.scheduleNext(nextTime(startTime, interval, datetimeToMillis(now)));
            entry.save();
        } catch (ReadValueException | InvalidArgumentException | EntryScheduleException | EntryStorageAccessException
                | ChangeValueException e) {
            throw new SchedulingStrategyExecutionException("Error occurred initializing scheduler entry.", e);
        }
    }

    @Override
    public void postProcess(final ISchedulerEntry entry) throws SchedulingStrategyExecutionException {
        try {
            Duration interval = Duration.parse((String) entry.getState().getValue(intervalFieldName));

            entry.scheduleNext(entry.getLastTime() + interval.toMillis());
        } catch (ReadValueException | InvalidArgumentException | EntryScheduleException e) {
            throw new SchedulingStrategyExecutionException("Error occurred rescheduling scheduler entry.", e);
        }
    }

    @Override
    public void restore(final ISchedulerEntry entry) throws SchedulingStrategyExecutionException {
        try {
            Duration interval = Duration.parse((String) entry.getState().getValue(intervalFieldName));
            LocalDateTime startTime = LocalDateTime.parse((String) entry.getState().getValue(startFieldName));

            long nextTime = nextTime(startTime, interval, System.currentTimeMillis());

            entry.scheduleNext(nextTime);
        } catch (ReadValueException | InvalidArgumentException | EntryScheduleException e) {
            throw new SchedulingStrategyExecutionException("Error occurred restoring scheduler entry.", e);
        }
    }

    @Override
    public void processException(final ISchedulerEntry entry, final Throwable e) throws SchedulingStrategyExecutionException {
        try {
            entry.cancel();
        } catch (EntryStorageAccessException | EntryScheduleException ee) {
            throw new SchedulingStrategyExecutionException("Error occurred cancelling failed scheduler entry.", ee);
        }
    }
}
