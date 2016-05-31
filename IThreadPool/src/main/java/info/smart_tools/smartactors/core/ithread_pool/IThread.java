package info.smart_tools.smartactors.core.ithread_pool;

import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;

/**
 * Thread that can execute task (only one at time).
 */
public interface IThread {
    /**
     * Execute given task and return to pool.
     *
     * @param task the task to execute
     * @throws TaskExecutionException if the thread is executing another task.
     */
    void execute(ITask task) throws TaskExecutionException;
}
