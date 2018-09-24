package info.smart_tools.smartactors.task.non_blocking_queue;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.class_management.version_manager.VersionManager;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Non-blocking implementation of {@link IQueue}.
 *
 * @param <T> type of elements
 */
public class NonBlockingQueue<T> implements IQueue<T> {

    private class Agregator {
        T item;
        IScope scope;
        Object moduleId;
    }

    private final Queue<Agregator> queue;
    private final List<Runnable> newElementCallbacks;
    private final Object callbacksListLock;

    /**
     * The constructor.
     *
     * @param queue    underlying standard queue
     * @throws InvalidArgumentException if queue queue is {@code null}
     */
    public NonBlockingQueue(final Queue<T> queue)
            throws InvalidArgumentException {
        if (null == queue) {
            throw new InvalidArgumentException("Internal queue may not be null.");
        }

        try {
            this.queue = queue.getClass().newInstance();

            T item;
            while( (item = queue.poll()) != null) {
                Agregator a = new Agregator();
                a.item = item;
                a.scope = ScopeProvider.getCurrentScope();
                a.moduleId = VersionManager.getCurrentModule();
                this.queue.add(a);
            }
            this.newElementCallbacks = new CopyOnWriteArrayList<>();
            this.callbacksListLock = new Object();
        } catch (IllegalAccessException | InstantiationException | ScopeProviderException e) {
            throw new InvalidArgumentException(e);
        }
    }

    @Override
    public void put(final T item) throws InterruptedException {
        Agregator a = new Agregator();
        a.item = item;
        try {
            a.scope = ScopeProvider.getCurrentScope();
        } catch(ScopeProviderException e) {
            throw new InterruptedException(e.getMessage());
        }
        a.moduleId = VersionManager.getCurrentModule();
        queue.add(a);

        for (Runnable callback : newElementCallbacks) {
            callback.run();
        }
    }

    @Override
    public T take() throws InterruptedException {
        throw new UnsupportedOperationException("Blocking take operation is not supported.");
    }

    @Override
    public T tryTake() {
        Agregator a = queue.poll();
        if (a == null) {
            return null;
        }
        try {
            ScopeProvider.setCurrentScope(a.scope);
        } catch(ScopeProviderException e) {
            throw new RuntimeException(e);
        }
        VersionManager.setCurrentModule(a.moduleId);
        return a.item;
    }

    @Override
    public void addNewItemCallback(final Runnable callback) {
        synchronized (callbacksListLock) {
            newElementCallbacks.add(callback);

            if (!queue.isEmpty()) {
                callback.run();
            }
        }
    }

    @Override
    public void removeNewItemCallback(final Runnable callback) {
        synchronized (callbacksListLock) {
            newElementCallbacks.remove(callback);
        }
    }
}
