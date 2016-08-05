package info.smart_tools.smartactors.core.feature_manager;

import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ipath.IPath;

import java.util.Collection;

/**
 * Class for storing to the queue of {@link FeatureManager}
 */
class ExecutionPair {
    private IAction<Collection<IPath>> action;
    private Collection<IPath> path;

    /**
     * Constructor.
     * Creates new instance of {@link ExecutionPair} by given action and path
     * @param action the instance of {@link IAction}
     * @param path the collection of {@link IPath}
     */
    ExecutionPair(final IAction<Collection<IPath>> action, final Collection<IPath> path) {
        this.action = action;
        this.path = path;
    }

    /**
     * Execute current instance of {@link IAction} with current collection of {@link Path}
     * @throws ActionExecuteException if execution of action has been failed
     */
    void execute() throws ActionExecuteException {
        try {
            this.action.execute(path);
        } catch (Exception e) {
            throw new ActionExecuteException(e);
        }
    }
}