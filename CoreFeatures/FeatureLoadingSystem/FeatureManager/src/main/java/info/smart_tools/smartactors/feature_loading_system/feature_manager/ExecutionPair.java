package info.smart_tools.smartactors.feature_loading_system.feature_manager;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.interfaces.ipath.IPath;

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