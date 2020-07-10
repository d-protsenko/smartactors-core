package info.smart_tools.smartactors.base.up_counter;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.IActionNoArgs;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecutionException;
import info.smart_tools.smartactors.base.iup_counter.IUpCounter;
import info.smart_tools.smartactors.base.iup_counter.exception.IllegalUpCounterState;
import info.smart_tools.smartactors.base.iup_counter.exception.UpCounterCallbackExecutionException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of {@link IUpCounter}.
 */
public class UpCounter implements IUpCounter {
    /**
     * <pre>
     * = 0 => no processes running, system can shutdown immediately
     * > 0 => there are running processes, cannot shutdown immediately
     * < 0 => system is down, cannot increment counter
     * </pre>
     */
    private final AtomicLong counter = new AtomicLong(0);
    private Object shutdownMode = null;

    private final Map<Object,IAction<Object>> shutdownRequestCallbacks = new ConcurrentHashMap<>();
    private final Map<Object,IAction<Void>> shutdownCompletionCallbacks = new ConcurrentHashMap<>();

    /**
     * The constructor.
     *
     * @param parent    the only parent counter of this one
     * @throws IllegalUpCounterState if parent is already down
     */
    public UpCounter(final IUpCounter parent) throws IllegalUpCounterState {
        // TODO:: Add addParent(IUpCounter) method to interface instead? (may cause loops, but may be useful)
        addParent(parent);
    }

    /**
     * Default constructor.
     */
    public UpCounter() {
    }

    /**
     * @param parent     the counter to be parent of this
     * @throws IllegalUpCounterState if that counter is already down
     */
    private void addParent(final IUpCounter parent) throws IllegalUpCounterState {
        parent.up();

        try {
            parent.onShutdownRequest(this.toString(), mode -> {
                try {
                    this.shutdown(mode);
                } catch (Exception e) {
                    throw new ActionExecutionException(e);
                }
            });

            // Execute shutdown callbacks even in case of force shutdown of parent counter.
            // May cause memory leaks if up-counters are created very frequently.
            parent.onShutdownComplete(this.toString(), () -> {
                try {
                    forceShutdown();
                } catch (IllegalUpCounterState ignore) {
                    // This counter is already down, ok
                } catch (Exception e) {
                    throw new ActionExecutionException(e);
                }
            });

            onShutdownComplete(this.toString(), () -> {
                try {
                    parent.down();
                } catch (Exception e) {
                    throw new ActionExecutionException(e);
                }
            });
        } catch (Exception e) {
            try {
                parent.down();
            } catch (Exception ee) {
                e.addSuppressed(ee);
            }

            throw new IllegalUpCounterState(e);
        }
    }

    /**
     * Execute _all_ of given callbacks (even if some throw).
     *
     * @throws UpCounterCallbackExecutionException if at least one of callbacks throws; exceptions thrown by subsequent callbacks are
     *                                             suppressed
     */
    private <T> void executeCallbacks(final Map<Object,IAction<T>> callbacks, final T arg) throws UpCounterCallbackExecutionException {
        UpCounterCallbackExecutionException exception = null;

        for (IAction<T> cb : callbacks.values()) {
            try {
                cb.execute(arg);
            } catch (Exception e) {
                if (exception == null) {
                    exception = new UpCounterCallbackExecutionException(e);
                } else {
                    exception.addSuppressed(e);
                }
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    private boolean tryShutdown(final long expect) throws UpCounterCallbackExecutionException {
        if (counter.compareAndSet(expect, -1)) {
            executeCallbacks(shutdownCompletionCallbacks, null);
            return true;
        }

        return false;
    }

    @Override
    public void up() throws IllegalUpCounterState {
        long ctr;

        do {
            ctr = counter.get();

            if (ctr < 0) {
                throw new IllegalUpCounterState("The system is already down.");
            }
        } while (!counter.compareAndSet(ctr, ctr + 1));
    }

    @Override
    public void down() throws UpCounterCallbackExecutionException, IllegalUpCounterState {
        long ctr;

        do {
            ctr = counter.get();

            if (ctr < 0) {
                throw new IllegalUpCounterState("The system is already down.");
            }

            if (ctr == 0) {
                throw new IllegalUpCounterState("There are no processes running.");
            }

            if (ctr == 1 && shutdownMode != null) {
                if (tryShutdown(1)) {
                    return;
                }
            }
        } while (!counter.compareAndSet(ctr, ctr - 1));
    }

    @Override
    public void shutdown(final Object mode) throws UpCounterCallbackExecutionException, IllegalUpCounterState {
        shutdownMode = mode;

        try {
            executeCallbacks(shutdownRequestCallbacks, mode);
        } finally {
            tryShutdown(0);
        }
    }

    @Override
    public void forceShutdown() throws UpCounterCallbackExecutionException, IllegalUpCounterState {
        long ctr;

        do {
            ctr = counter.get();

            if (ctr < 0) {
                throw new IllegalUpCounterState("System is already down.");
            }
        } while (!tryShutdown(ctr));
    }

    @Override
    public IAction<Object> onShutdownRequest(final Object key, final IAction<Object> callback) throws UpCounterCallbackExecutionException {
        return shutdownRequestCallbacks.put(key, callback);
    }

    @Override
    public IAction<Object> removeFromShutdownRequest(final Object key) {
        return shutdownRequestCallbacks.remove(key);
    }

    @Override
    public IActionNoArgs onShutdownComplete(final Object key, final IActionNoArgs callback)
            throws UpCounterCallbackExecutionException {
        IAction<Void> action = shutdownCompletionCallbacks.put(key, __ -> callback.execute());
        if (null == action) {
            return null;
        } else {
            return () -> {
                try {
                    action.execute(null);
                } catch (InvalidArgumentException e) {
                    // underlying IActionNoArgs doesn't throw InvalidArgumentException
                }
            };
        }
    }

    @Override
    public IActionNoArgs removeFromShutdownComplete(final Object key) {
        IAction<Void> action = shutdownCompletionCallbacks.remove(key);
        return  () -> {
            try {
                action.execute(null);
            } catch (InvalidArgumentException e) {
                // underlying IActionNoArgs doesn't throw InvalidArgumentException
            }
        };
    }
}
