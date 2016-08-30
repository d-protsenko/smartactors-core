package info.smart_tools.smartactors.core.feature_loader;

import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ifeature_loader.IFeatureStatus;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ipath.IPath;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 */
public class FeatureStatusImpl implements IFeatureStatus {
    private final String id;
    private IPath path;
    private boolean isCompleted = false;
    private Throwable error = null;
    private final ConcurrentLinkedQueue<IAction<Throwable>> completionCallbacks = new ConcurrentLinkedQueue<>();
    private List<FeatureStatusImpl> dependencies = new LinkedList<>();

    private List<IPath> jars;
    private IObject config;

    /**
     * The constructor.
     *
     * @param id    identifier of the feature
     */
    FeatureStatusImpl(final String id) {
        this.id = id;
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

    /**
     * Add status of feature this one depends on.
     *
     * @param feature    a feature this one depends on
     */
    void addDependency(final FeatureStatusImpl feature) {
        dependencies.add(feature);
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
        return jars != null && config != null;
    }

    /**
     * Initialize directory path, jar's list and configuration object of the feature.
     *
     * @param dirPath      path of feature's directory
     * @param jarPaths     paths of the {@code .jar} files of the feature
     * @param theConfig    configuration object of the feature
     */
    void init(final IPath dirPath, final List<IPath> jarPaths, final IObject theConfig) {
        path = dirPath;
        jars = jarPaths;
        config = theConfig;
    }
}
