package info.smart_tools.smartactors.feature_loader.feature_loader;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.IBiAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.interfaces.ipath.IPath;
import info.smart_tools.smartactors.feature_loader.interfaces.ifeature_loader.IFeatureStatus;
import info.smart_tools.smartactors.feature_loader.interfaces.ifeature_loader.exceptions.FeatureLoadException;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class FeatureStatusImpl implements IFeatureStatus {
    private final String id;
    private IBiAction<IObject, IPath> loadAction;
    private IPath path = null;
    private boolean isCompleted = false;
    private Throwable error = null;
    private final ConcurrentLinkedQueue<IAction<Throwable>> completionCallbacks = new ConcurrentLinkedQueue<>();

    private List<FeatureStatusImpl> dependencies = new LinkedList<>();
    private IObject config = null;

    private final AtomicInteger nExpect = new AtomicInteger(1);

    /**
     * The constructor.
     *
     * @param id            identifier of the feature
     * @param loadAction    action loading plugins and configuration of the feature
     * @throws InvalidArgumentException if {@code id} is {@code null}
     * @throws InvalidArgumentException if {@code loadAction} is {@code null}
     */
    public FeatureStatusImpl(final String id, final IBiAction<IObject, IPath> loadAction)
            throws InvalidArgumentException {
        if (null == id) {
            throw new InvalidArgumentException("Feature id should not be null.");
        }

        if (null == loadAction) {
            throw new InvalidArgumentException("Load action should not be null.");
        }

        this.id = id;
        this.loadAction = loadAction;
    }

    @Override
    public boolean isLoaded() {
        return isCompleted && (error == null);
    }

    @Override
    public boolean isFailed() {
        return isCompleted && (error != null);
    }

    @Override
    public IPath getPath() {
        return path;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void whenDone(final IAction<Throwable> action)
            throws ActionExecuteException {
        completionCallbacks.add(action);
        if (isCompleted) {
            runCallbacks();
        }
    }

    private void runCallbacks()
            throws ActionExecuteException {
        IAction<Throwable> action;
        Throwable callbackException = null;

        while (null != (action = completionCallbacks.poll())) {
            try {
                action.execute(error);
            } catch (Throwable e) {
                if (callbackException == null) {
                    callbackException = e;
                } else {
                    callbackException.addSuppressed(e);
                }
            }
        }

        if (null != callbackException) {
            throw new ActionExecuteException(callbackException);
        }
    }

    private void decreaseCounter()
            throws ActionExecuteException {
        if (isCompleted || nExpect.decrementAndGet() != 0) {
            return;
        }

        try {
            IQueue<ITask> taskQueue = IOC.resolve(Keys.getOrAdd("task_queue"));

            taskQueue.put(() -> {
                try {
                    loadAction.execute(config, path);
                } catch (Exception e) {
                    error = e;
                } finally {
                    isCompleted = true;
                    try {
                        runCallbacks();
                    } catch (ActionExecuteException e) {
                        System.err.println(new java.util.Date());
                        e.printStackTrace();
                    }
                }
            });
        } catch (Throwable e) {
            error = e;
            System.err.println(new java.util.Date());
            e.printStackTrace();
        }
    }

    private void failDependency(final String depId, final Throwable err) {
        error = new FeatureLoadException(MessageFormat.format("Error loading dependency named ''{0}''.", depId), err);
        isCompleted = true;
        try {
            runCallbacks();
        } catch (ActionExecuteException e) {
            error.addSuppressed(e);
        }
    }

    /**
     * Add status of feature this one depends on.
     *
     * @param feature    a feature this one depends on
     */
    void addDependency(final FeatureStatusImpl feature) {
        dependencies.add(feature);
        nExpect.incrementAndGet();
        try {
            feature.whenDone(error -> {
                if (error != null) {
                    failDependency(feature.getId(), error);
                } else {
                    decreaseCounter();
                }
            });
        } catch (ActionExecuteException e) {
            failDependency(feature.getId(), e);
        }
    }

    /**
     * Get dependencies added by {@link #addDependency(FeatureStatusImpl)}.
     *
     * @return statuses of the features this one depends on
     */
    List<FeatureStatusImpl> getDependencies() {
        return dependencies;
    }

    boolean isInitialized() {
        return config != null && path != null;
    }

    /**
     * Initialize directory path, jar's list and configuration object of the feature.
     *
     * @param dirPath      path of feature's directory
     * @param theConfig    configuration object of the feature
     */
    void init(final IPath dirPath, final IObject theConfig) {
        path = dirPath;
        config = theConfig;
    }

    /**
     * Load the feature or await for dependencies to be loaded.
     *
     * @throws ActionExecuteException if error occurs calling callbacks
     */
    void load()
            throws ActionExecuteException {
        decreaseCounter();
    }
}
