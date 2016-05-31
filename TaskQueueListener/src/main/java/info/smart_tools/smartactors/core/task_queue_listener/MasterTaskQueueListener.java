package info.smart_tools.smartactors.core.task_queue_listener;

import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.ithread_pool.IThread;
import info.smart_tools.smartactors.core.ithread_pool.IThreadPool;

/**
 * The task observing the queue using blocking operation ({@link IQueue#take()}).
 */
public class MasterTaskQueueListener implements ITask {
    private final IQueue<ITask> queue;
    private final IThreadPool threadPool;
    private final ITask slave;

    /**
     * The constructor.
     *
     * @param queue the queue to take tasks from
     * @param threadPool the thread pool to use
     * @throws IllegalArgumentException if queue is {@code null}
     * @throws IllegalArgumentException if thread pool is {@code null}
     */
    public MasterTaskQueueListener(final IQueue<ITask> queue, final  IThreadPool threadPool)
            throws IllegalArgumentException {
        if (null == queue) {
            throw new IllegalArgumentException("Queue should not be null.");
        }

        if (null == threadPool) {
            throw new IllegalArgumentException("Thread pool should not be null.");
        }

        this.queue = queue;
        this.threadPool = threadPool;
        this.slave = new SlaveTaskQueueListener(queue, threadPool);
    }

    @Override
    public void execute() throws TaskExecutionException {
        while (!Thread.interrupted()) {
            try {
                ITask task = this.queue.take();

                try {
                    IThread thread = threadPool.getThread();

                    if (null != thread) {
                        thread.execute(slave);
                    }
                } catch (TaskExecutionException e) {
                    // TODO: Handle.
                }

                try {
                    task.execute();
                } catch (TaskExecutionException e) {
                    // TODO: Handle.
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
