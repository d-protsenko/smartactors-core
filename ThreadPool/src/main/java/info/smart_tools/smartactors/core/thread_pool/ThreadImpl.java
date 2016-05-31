package info.smart_tools.smartactors.core.thread_pool;

import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.ithread_pool.IThread;

import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public class ThreadImpl implements IThread {
    private final Thread thread;
    private final ThreadPool pool;
    private final AtomicReference<ITask> setTaskRef;
    private final Object lock;

    /**
     *
     */
    private class ThreadRunnable implements Runnable {
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    synchronized (lock) {
                        while (setTaskRef.get() == null) {
                            lock.wait();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    continue;
                }

                try {
                    setTaskRef.get().execute();
                } catch (TaskExecutionException e) {
                    // TODO: Handle
                }

                setTaskRef.set(null);
                pool.returnThread(ThreadImpl.this);
            }
        }
    }

    /**
     *
     * @param pool    the thread pool that owns this thread
     */
    public ThreadImpl(final ThreadPool pool) {
        this.pool = pool;

        this.setTaskRef = new AtomicReference<>(null);
        this.lock = new Object();

        this.thread = new Thread(new ThreadRunnable());

        this.thread.start();
    }

    @Override
    public void execute(final ITask task) throws TaskExecutionException {
        if (!setTaskRef.compareAndSet(null, task)) {
            throw new TaskExecutionException("Another task is being executed.");
        }

        if (!thread.isAlive()) {
            throw new TaskExecutionException("Thread is dead.");
        }

        synchronized (this.lock) {
            this.lock.notifyAll();
        }
    }
}
