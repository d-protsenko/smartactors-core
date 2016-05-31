package info.smart_tools.smartactors.core.thread_pool;

import info.smart_tools.smartactors.core.ithread_pool.IThread;
import info.smart_tools.smartactors.core.ithread_pool.IThreadPool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 *
 */
public class ThreadPool implements IThreadPool {
    private final BlockingQueue<ThreadImpl> threadsQueue;

    /**
     *
     * @param threadCount    initial count of threads.
     */
    public ThreadPool(final int threadCount) {
        threadsQueue = new ArrayBlockingQueue<>(threadCount);

        for (int i = 0; i < threadCount; i++) {
            threadsQueue.offer(new ThreadImpl(this));
        }
    }

    @Override
    public IThread getThread() {
        return threadsQueue.poll();
    }

    /**
     *
     * @param thread the thread
     */
    void returnThread(final ThreadImpl thread) {
        threadsQueue.offer(thread);
    }
}
