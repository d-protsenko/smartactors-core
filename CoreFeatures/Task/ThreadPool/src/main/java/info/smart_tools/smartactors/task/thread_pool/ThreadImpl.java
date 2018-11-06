package info.smart_tools.smartactors.task.thread_pool;

import info.smart_tools.smartactors.class_management.module_manager.ModuleManager;
import info.smart_tools.smartactors.scope.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;

import java.util.concurrent.atomic.AtomicReference;

/**
 * The thread waiting for a task and returning itself to the {@link ThreadPool} when done.
 */
class ThreadImpl {
    private final Thread thread;
    private final ThreadPool pool;
    private final AtomicReference<ITask> setTaskRef;
    private final Object lock;

    /**
     * The {@link Runnable} that will run on Java thread.
     */
    private class ThreadRunnable implements Runnable {
        @Override
        public void run() {
            ModuleManager.setCurrentModule(pool.getModule());
            try {
                ScopeProvider.setCurrentScope(pool.getScope());
            } catch (ScopeProviderException e) {
                // TODO: Handle
                e.printStackTrace();
            }
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
                    e.printStackTrace();
                }

                setTaskRef.set(null);
                pool.returnThread(ThreadImpl.this);
            }
        }
    }

    /**
     * The constructor.
     *
     * @param pool          the thread pool that owns this thread
     * @param threadName    the name for thread to create
     */
    ThreadImpl(final ThreadPool pool, final String threadName) {
        this.pool = pool;

        this.setTaskRef = new AtomicReference<>(null);
        this.lock = new Object();

        this.thread = new Thread(new ThreadRunnable(), threadName);

        this.thread.start();
    }

    /**
     * Start execution of given task in this thread.
     *
     * @param task the task to execute.
     * @throws TaskExecutionException if another task is being executed on this thread
     * @throws TaskExecutionException if the thread is already not alive
     */
    void execute(final ITask task) throws TaskExecutionException {
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

    /**
     * Interrupt the underlying Java thread (using {@link Thread#interrupt()} method).
     */
    void interrupt() {
        this.thread.interrupt();
    }
}
