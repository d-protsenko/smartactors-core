package info.smart_tools.smartactors.core.dependency_resolving_feature_manager;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.interfaces.ipath.IPath;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ifeature_manager.IFeature;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ifeature_manager.exception.FeatureManagementException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of {@link IFeature} used by {@link DependencyResolvingFeatureManager}.
 */
class Feature implements IFeature {
    private final DependencyResolvingFeatureManager manager;
    private final String name;
    private final Object lock;
    private final List<IAction<Collection<IPath>>> listeners;
    private final List<String> required;
    private List<IPath> paths;

    /**
     * The constructor.
     *
     * @param manager    feature manager that created this feature
     * @param name       name of the feature
     * @throws InvalidArgumentException if {@code manager} is {@code null}
     * @throws InvalidArgumentException if {@code name} is {@code null}
     */
    Feature(final DependencyResolvingFeatureManager manager, final String name)
            throws InvalidArgumentException {
        if (null == manager) {
            throw new InvalidArgumentException("Manager should not be null.");
        }

        if (null == name) {
            throw new InvalidArgumentException("Name should not be null.");
        }

        this.manager = manager;
        this.name = name;

        lock = new Object();
        listeners = new LinkedList<>();
        required = new LinkedList<>();
        paths = null;
    }

    @Override
    public void requireFile(final String fileName) throws FeatureManagementException {
        synchronized (lock) {
            if (null != paths) {
                throw new FeatureManagementException("requireFile() called after listening started.");
            }

            required.add(fileName);
        }
    }

    @Override
    public void whenPresent(final IAction<Collection<IPath>> action) throws FeatureManagementException {
        synchronized (lock) {
            if (null == paths) {
                listeners.add(action);
            } else {
                try {
                    action.execute(paths);
                } catch (ActionExecuteException | InvalidArgumentException e) {
                    throw new FeatureManagementException("Error occurred executing action for a feature that already is present:", e);
                }
            }
        }
    }

    @Override
    public void listen() throws FeatureManagementException {
        synchronized (lock) {
            if (0 == required.size()) {
                throw new FeatureManagementException("No items defined for this feature.");
            }

            if (null != paths) {
                throw new FeatureManagementException("Items for this feature are already resolved (#listen called twice).");
            }

            this.paths = manager.resolveArtifacts(this.required);

            List<Throwable> exceptions = new LinkedList<>();

            for (IAction<Collection<IPath>> action : listeners) {
                try {
                    action.execute(paths);
                } catch (Exception e) {
                    exceptions.add(e);
                }
            }

            if (exceptions.size() != 0) {
                Throwable e = exceptions.remove(0);

                for (Throwable ee : exceptions) {
                    e.addSuppressed(ee);
                }

                throw new FeatureManagementException("Error(s) occurred notifying feature listeners.", e);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
