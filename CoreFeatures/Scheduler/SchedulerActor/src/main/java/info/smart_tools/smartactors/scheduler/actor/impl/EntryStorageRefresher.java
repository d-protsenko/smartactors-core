package info.smart_tools.smartactors.scheduler.actor.impl;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.scheduler.actor.impl.exceptions.CancelledLocalEntryRequestException;
import info.smart_tools.smartactors.scheduler.actor.impl.remote_storage.IRemoteEntryStorage;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.EntryScheduleException;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.EntryStorageAccessException;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.timer.interfaces.itimer.ITimer;
import info.smart_tools.smartactors.timer.interfaces.itimer.ITimerTask;
import info.smart_tools.smartactors.timer.interfaces.itimer.exceptions.TaskScheduleException;

import java.util.List;

/**
 *
 */
public class EntryStorageRefresher {
    private final EntryStorage entryStorage;
    private final IRemoteEntryStorage remoteEntryStorage;

    private final IQueue<ITask> taskQueue;
    private ITimerTask timerTask;
    private ITimer timer;

    private int pageSize;
    private long refreshRepeatInterval;
    private long refreshAwakeInterval;
    private long refreshSuspendInterval;
    private long refreshStart;

    private IObject lastDownloadedState;

    private final IFieldName entryIdFN;

    private final ITask downloadTask = this::doDownloadIteration;
    private final ITask localRefreshTask = this::doRefreshLocalStorage;

    /**
     * The constructor.
     *
     * <p>
     * Interval values should satisfy the following inequality:
     * {@code 0 < refreshRepeatInterval <= refreshAwakeInterval < refreshSuspendInterval}
     * </p>
     *
     * @param entryStorage              the entry storage
     * @param remoteEntryStorage        the remote entry storage
     * @param refreshRepeatInterval     interval between refresher executions in milliseconds
     * @param refreshAwakeInterval      time (in milliseconds after current execution start) to awake entries scheduled until
     * @param refreshSuspendInterval    time (in milliseconds after current execution start) to suspend entries scheduled after
     * @param pageSize                  size of pages downloaded from remote storage
     * @throws ResolutionException if error occurs resolving any dependency
     * @throws TaskScheduleException if error occurs scheduling first execution
     * @throws InvalidArgumentException if {@code entryStorage} or {@code remoteEntryStorage} are {@code null}
     * @throws InvalidArgumentException if interval values do not satisfy requirements described above
     * @throws InvalidArgumentException if {@code pageSize} is not a positive value
     */
    public EntryStorageRefresher(final EntryStorage entryStorage,
                                 final IRemoteEntryStorage remoteEntryStorage,
                                 final long refreshRepeatInterval,
                                 final long refreshAwakeInterval,
                                 final long refreshSuspendInterval,
                                 final int pageSize)
            throws ResolutionException, TaskScheduleException, InvalidArgumentException {
        if (null == entryStorage) {
            throw new InvalidArgumentException("Entry storage should not be null.");
        }

        if (null == remoteEntryStorage) {
            throw new InvalidArgumentException("Remote entry storage should not be null.");
        }

        if (!(
                (0 < refreshRepeatInterval) &&
                (refreshRepeatInterval <= refreshAwakeInterval) &&
                (refreshAwakeInterval < refreshSuspendInterval)
        )) {
            throw new InvalidArgumentException(String.format(
                    "Invalid interval values: RRI = %d, RAI = %d, RSI = %d; should be: 0 < RRI <= RAI < RSI.",
                    refreshRepeatInterval, refreshAwakeInterval, refreshSuspendInterval));
        }

        if (pageSize <= 0) {
            throw new InvalidArgumentException("Invalid page size.");
        }

        this.entryStorage = entryStorage;
        this.remoteEntryStorage = remoteEntryStorage;

        this.refreshRepeatInterval = refreshRepeatInterval;
        this.refreshAwakeInterval = refreshAwakeInterval;
        this.refreshSuspendInterval = refreshSuspendInterval;

        this.pageSize = pageSize;

        this.taskQueue = IOC.resolve(Keys.getOrAdd("task_queue"));

        this.entryIdFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "entryId");

        timer = IOC.resolve(Keys.getOrAdd("timer"));
    }

    public void start() throws TaskScheduleException {
        this.refreshStart = System.currentTimeMillis();

        this.timerTask = timer.schedule(this::startRefresh, refreshStart);
    }

    private void startRefresh() throws TaskExecutionException {
        try {
            lastDownloadedState = null;
            taskQueue.put(downloadTask);
        } catch (InterruptedException e) {
            throw new TaskExecutionException("Unexpected interrupt while refreshing scheduler entry storage.", e);
        }
    }

    private void doDownloadIteration() throws TaskExecutionException {
        try {
            boolean cont = true;
            try {
                List<IObject> entries = remoteEntryStorage.downloadEntries(refreshStart + refreshRepeatInterval, lastDownloadedState, pageSize);

                for (IObject entryState : entries) {
                    String id = (String) entryState.getValue(entryIdFN);

                    try {
                        ISchedulerEntry localEntry = entryStorage.getLocalEntry(id);

                        if (null == localEntry) {
                            ISchedulerEntry newEntry = IOC.resolve(Keys.getOrAdd("restore scheduler entry"), entryState, entryStorage);
                            remoteEntryStorage.weakSaveEntry(newEntry);
                        } else {
                            entryStorage.notifyActive(localEntry);
                        }
                    } catch (CancelledLocalEntryRequestException e) {
                        continue;
                    }

                    lastDownloadedState = entryState;
                }

                cont = !entries.isEmpty();
            } finally {
                taskQueue.put(cont ? downloadTask : localRefreshTask);
            }
        } catch (Exception e) {
            throw new TaskExecutionException(e);
        }
    }

    private void doRefreshLocalStorage() throws TaskExecutionException {
        try {
            try {
                entryStorage.refresh(refreshStart + refreshAwakeInterval, refreshStart + refreshSuspendInterval);
            } finally {
                refreshStart = refreshStart + refreshRepeatInterval;
                timerTask.reschedule(refreshStart);
            }
        } catch (EntryStorageAccessException | EntryScheduleException | TaskScheduleException e) {
            throw new TaskExecutionException(e);
        }
    }
}
