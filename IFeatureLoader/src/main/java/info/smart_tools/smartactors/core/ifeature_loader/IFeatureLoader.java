package info.smart_tools.smartactors.core.ifeature_loader;

import info.smart_tools.smartactors.core.ifeature_loader.exceptions.FeatureLoadException;
import info.smart_tools.smartactors.core.ipath.IPath;

/**
 * Feature loader.
 */
public interface IFeatureLoader {
    /**
     * Load group of features.
     *
     * <p>
     *     Directory of a feature group should contain directories of features.
     * </p>
     *
     * @param groupPath    path to the group directory
     * @return status of the feature including all the features of the feature group
     * @throws FeatureLoadException if any error occurs
     */
    IFeatureStatus loadGroup(final IPath groupPath) throws FeatureLoadException;

    /**
     * Load feature from given directory.
     *
     * <p>
     *      A feature directory should contain a <em>feature configuration file</em> named {@code "config.json"} containing the following
     *      fields:
     *      <ul>
     *          <li>{@code "featureId"} - identifier of the feature</li>
     *          <li>{@code "afterFeatures"} - array of identifiers of features that should be loaded before that feature</li>
     *      </ul>
     *      Also the directory may contain {@code *.jar} files of plugins required by the feature and their dependencies.
     * </p>
     *
     * @param featurePath     path to the directory
     * @return status of the feature
     * @throws FeatureLoadException if any error occurs
     */
    IFeatureStatus loadFeature(final IPath featurePath) throws FeatureLoadException;

    /**
     * Get status of feature with given identifier.
     *
     * @param featureId    identifier of the feature
     * @return status of the feature
     */
    IFeatureStatus getFeatureStatus(final String featureId);
}
