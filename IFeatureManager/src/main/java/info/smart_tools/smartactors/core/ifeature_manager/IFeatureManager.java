package info.smart_tools.smartactors.core.ifeature_manager;

import info.smart_tools.smartactors.core.ifeature_manager.exception.FeatureManagementException;
import info.smart_tools.smartactors.core.ifilesystem_tracker.IFilesystemTracker;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;

/**
 *
 */
public interface IFeatureManager {
    /**
     * Create new {@link IFeature} instance managed by this manager.
     *
     * @param name the name of new feature
     * @param tracker the instance of {@link IFilesystemTracker}
     * @return the new {@link IFeature} instance.
     * @throws FeatureManagementException if any error occurs.
     * @throws InvalidArgumentException if incoming arguments are incorrect
     */
    IFeature newFeature(String name, IFilesystemTracker tracker)
            throws FeatureManagementException, InvalidArgumentException;
}
