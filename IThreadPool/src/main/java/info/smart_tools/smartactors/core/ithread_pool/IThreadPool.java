package info.smart_tools.smartactors.core.ithread_pool;

/**
 * Thread pool.
 */
public interface IThreadPool {
    /**
     * Get a thread ready to execute a task. Returns {@code null} if there is no such thread.
     *
     * @return the thread or {@code null}
     */
    IThread getThread();
}
