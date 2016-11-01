package info.smart_tools.smartactors.checkpoint.scheduling_strategies;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulingStrategy;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.EntryScheduleException;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.EntryStorageAccessException;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.SchedulingStrategyExecutionException;

import java.time.Duration;
import java.time.format.DateTimeParseException;

/**
 *
 *
 * <pre>
 * {
 *   "strategy": "checkpoint repeat strategy",
 *   "interval": "PT3H", //Interval in ISO-8601 format
 *   "times": 3 // How many times to re-send the message
 * }
 * </pre>
 */
public class CheckpointRepeatStrategy implements ISchedulingStrategy {
    private final IFieldName intervalFieldName;
    private final IFieldName timesFieldName;
    private final IFieldName remainingTimesFieldName;
    private final IFieldName postRestoreDelayFieldName;
    private final IFieldName postCompletionDelayFieldName;
    private final IFieldName completedFieldName;

    /**
     * The constructor.
     *
     * @throws ResolutionException if error occurs resolving any dependencies
     */
    public CheckpointRepeatStrategy()
            throws ResolutionException {
        intervalFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "interval");
        timesFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "times");
        remainingTimesFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "remainingTimes");
        postRestoreDelayFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "postRestoreDelay");
        postCompletionDelayFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "postCompletionDelay");
        completedFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "completed");
    }

    @Override
    public void init(final ISchedulerEntry entry, final IObject args) throws SchedulingStrategyExecutionException {
        try {
            int times = ((Number) args.getValue(timesFieldName)).intValue();

            if (times == 0) {
                return;
            }

            Duration interval = Duration.parse((String) args.getValue(intervalFieldName));
            Duration postRestoreDelay = (args.getValue(postRestoreDelayFieldName) != null)
                    ? Duration.parse((String) args.getValue(postRestoreDelayFieldName)) : interval;
            Duration postCompletionDelay = (args.getValue(postCompletionDelayFieldName) != null)
                    ? Duration.parse((String) args.getValue(postCompletionDelayFieldName)) : interval;

            IObject state = entry.getState();

            state.setValue(intervalFieldName, interval.toString());
            state.setValue(postRestoreDelayFieldName, postRestoreDelay.toString());
            state.setValue(postCompletionDelayFieldName, postCompletionDelay.toString());
            state.setValue(timesFieldName, times);
            state.setValue(remainingTimesFieldName, times - 1);

            entry.save();

            entry.scheduleNext(System.currentTimeMillis() + interval.toMillis());
        } catch (ReadValueException | InvalidArgumentException | ChangeValueException | DateTimeParseException
                | EntryStorageAccessException | EntryScheduleException e) {
            throw new SchedulingStrategyExecutionException("Error occurred initializing scheduling strategy.", e);
        }
    }

    @Override
    public void postProcess(final ISchedulerEntry entry) throws SchedulingStrategyExecutionException {
        try {
            int remainingTimes = ((Number) entry.getState().getValue(remainingTimesFieldName)).intValue();
            long scheduleTime = System.currentTimeMillis();

            if (null != entry.getState().getValue(completedFieldName)) {
                if (0 == remainingTimes) {
                    entry.cancel();
                    return;
                } else {
                    // The was trials remaining but checkpoint actor marked the entry as completed because of received feedback message
                    entry.getState().setValue(remainingTimesFieldName, 0);
                    Duration delay = Duration.parse((String) entry.getState().getValue(postCompletionDelayFieldName));
                    scheduleTime += delay.toMillis();
                }
            } else {
                if (0 == remainingTimes) {
                    entry.getState().setValue(completedFieldName, true);
                    Duration delay = Duration.parse((String) entry.getState().getValue(postCompletionDelayFieldName));
                    scheduleTime += delay.toMillis();
                } else {
                    Duration interval = Duration.parse((String) entry.getState().getValue(intervalFieldName));
                    entry.getState().setValue(remainingTimesFieldName, remainingTimes - 1);
                    scheduleTime += interval.toMillis();
                }
            }

            entry.save();
            entry.scheduleNext(scheduleTime);
        } catch (ReadValueException | InvalidArgumentException | ChangeValueException | EntryScheduleException
                | EntryStorageAccessException e) {
            throw new SchedulingStrategyExecutionException("Error occurred post processing checkpoint scheduler entry.", e);
        }
    }

    @Override
    public void restore(final ISchedulerEntry entry) throws SchedulingStrategyExecutionException {
        try {
            long scheduleTime = System.currentTimeMillis();

            if (null != entry.getState().getValue(completedFieldName)) {
                entry.getState().setValue(remainingTimesFieldName, 0);
                Duration delay = Duration.parse((String) entry.getState().getValue(postCompletionDelayFieldName));
                scheduleTime += delay.toMillis();
            } else {
                Duration delay = Duration.parse((String) entry.getState().getValue(postRestoreDelayFieldName));
                scheduleTime += delay.toMillis();
            }

            entry.scheduleNext(scheduleTime);
        } catch (ReadValueException | InvalidArgumentException | ChangeValueException | EntryScheduleException e) {
            throw new SchedulingStrategyExecutionException("Error occurred restoring checkpoint scheduler entry.", e);
        }
    }

    @Override
    public void processException(final ISchedulerEntry entry, final Throwable e) throws SchedulingStrategyExecutionException {
        try {
            // TODO: Handle another way (?)
            entry.cancel();
        } catch (EntryScheduleException | EntryStorageAccessException e1) {
            e1.addSuppressed(e);
            throw new SchedulingStrategyExecutionException("Error occurred cancelling failed checkpoint scheduler entry.", e1);
        }
    }
}
