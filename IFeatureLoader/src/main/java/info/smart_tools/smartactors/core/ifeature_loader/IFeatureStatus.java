package info.smart_tools.smartactors.core.ifeature_loader;

import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ipath.IPath;

/**
 * Feature status.
 */
public interface IFeatureStatus {
    /**
     * True if the feature is successfully loaded.
     *
     * @return {@code true} if feature is successfully loaded, {@code false} otherwise
     */
    boolean isLoaded();

    /**
     * True if the feature load is failed.
     *
     * @return {@code true} if feature load is failed, {@code false} otherwise
     */
    boolean isFailed();

    /**
     * Get path of the directory where files of the feature are located.
     *
     * @return path of feature directory
     */
    IPath getPath();

    /**
     * Get the identifier of the feature.
     *
     * @return identifier of the feature
     */
    String getId();

    /**
     * Execute the given action when feature load is completed.
     *
     * <p>
     *     Callback is executed with {@code null} as a argument if load completed successful and with a exception if load is failed.
     * </p>
     *
     * <p>
     *     Callback is executed synchronously if load of the feature is completed at the moment of {@code #whenDone} call.
     * </p>
     *
     * @param action    the action to execute when feature load will be completed
     * @throws ActionExecuteException if synchronous execution of callback throws exception
     */
    void whenDone(final IAction<Throwable> action) throws ActionExecuteException;
}
