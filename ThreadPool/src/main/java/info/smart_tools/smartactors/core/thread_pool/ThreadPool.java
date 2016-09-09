package info.smart_tools.smartactors.core.thread_pool;

import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.ithread_pool.IThreadPool;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Implementation of {@link IThreadPool}.
 */
public class ThreadPool implements IThreadPool {
    private final Queue<ThreadImpl> threadsQueue;
    private IScope scope;

    /**
     * The constructor.
     *
     * @param threadCount    initial count of threads.
     */
    public ThreadPool(final int threadCount) {
        threadsQueue = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < threadCount; i++) {
            threadsQueue.offer(new ThreadImpl(this));
        }

        try {
            this.scope = ScopeProvider.getCurrentScope();
        } catch (ScopeProviderException e) {
            this.scope = null;
        }
    }

    @Override
    public boolean tryExecute(final ITask task)
            throws TaskExecutionException {
        ThreadImpl thread = threadsQueue.poll();

        if (null != thread) {
            thread.execute(task);
            return true;
        }

        return false;
    }

    /**
     * Returns the thread to this pool.
     *
     * @param thread the thread
     */
    void returnThread(final ThreadImpl thread) {
        if (!threadsQueue.offer(thread)) {
            thread.interrupt();
        }
    }

    IScope getScope() {
        return this.scope;
    }
}
