package info.smart_tools.smartactors.core.task_queue_listener;

import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.ithread_pool.IThread;
import info.smart_tools.smartactors.core.ithread_pool.IThreadPool;

/**
 *
 */
public class SlaveTaskQueueListener implements ITask {
    private final IQueue<ITask> queue;
    private final IThreadPool threadPool;

    /**
     * The constructor.
     *
     * @param queue the queue to take tasks from
     * @param threadPool the thread pool to use
     * @throws IllegalArgumentException if queue is {@code null}
     * @throws IllegalArgumentException if thread pool is {@code null}
     */
    public SlaveTaskQueueListener(final IQueue<ITask> queue, final  IThreadPool threadPool)
            throws IllegalArgumentException {
        if (null == queue) {
            throw new IllegalArgumentException("Queue should not be null.");
        }

        if (null == threadPool) {
            throw new IllegalArgumentException("Thread pool should not be null.");
        }

        this.queue = queue;
        this.threadPool = threadPool;
    }

    @Override
    public void execute() throws TaskExecutionException {
        for (;;) {
            ITask task = queue.tryTake();
            IThread thread = null;

            if (null == task) {
                break;
            }

            try {
                thread = threadPool.getThread();

                if (null != thread) {
                    thread.execute(this);
                }
            } catch (TaskExecutionException e) {
                // TODO: Handle.
            }

            try {
                task.execute();
            } catch (TaskExecutionException e) {
                // TODO: Handle.
            }

            if (null != thread) {
                break;
            }
        }
    }
}
