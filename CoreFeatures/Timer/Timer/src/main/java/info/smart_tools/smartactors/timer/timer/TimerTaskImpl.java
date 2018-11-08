package info.smart_tools.smartactors.timer.timer;

import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.timer.interfaces.itimer.ITimerTask;
import info.smart_tools.smartactors.timer.interfaces.itimer.exceptions.TaskScheduleException;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Implementation of {@link ITimerTask}.
 */
final class TimerTaskImpl implements ITimerTask {
    private TimerTask timerTask;
    private final ITask task;
    private final Timer timer;

    /**
     * Implementation of standard {@link TimerTask} that enqueues a task to task queue.
     */
    private class JTimerTaskImpl extends TimerTask {
        private IQueue<ITask> taskQueue;

        JTimerTaskImpl(final IQueue<ITask> taskQueue) {
            this.taskQueue = taskQueue;
        }

        @Override
        public void run() {
            try {
                taskQueue.put(task);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * The constructor.
     *
     * @param task            the task
     * @param timer           the java timer to use
     * @param firstRunTime    the time (in milliseconds since epoch) to run the task first time
     * @throws TaskScheduleException if fails to schedule the task
     */
    TimerTaskImpl(final ITask task, final Timer timer, final long firstRunTime)
            throws TaskScheduleException {
        this.task = task;
        this.timer = timer;

        reschedule(firstRunTime);
    }

    @Override
    public boolean cancel() {
        boolean res = (null != timerTask) && timerTask.cancel();
        timerTask = null;
        return res;
    }

    @Override
    public void reschedule(final long time) throws TaskScheduleException {
        cancel();

        try {
            IQueue<ITask> taskQueue = IOC.resolve(Keys.resolveByName("task_queue"));
            timerTask = new JTimerTaskImpl(taskQueue);
            long delay = time - System.currentTimeMillis();
            timer.schedule(timerTask, delay > 0 ? delay : 0);
        } catch (ResolutionException e) {
            throw new TaskScheduleException("Could not schedule a task.", e);
        }
    }
}
