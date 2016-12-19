package info.smart_tools.smartactors.feature_management.interfaces.ifeature;

import info.smart_tools.smartactors.base.interfaces.ipath.IPath;

import java.util.Set;

/**
 * IFeature interface
 */
public interface IFeature {

    /**
     * Gets feature name
     * @return the feature name
     */
    String getName();

    /**
     * Gets set of the feature dependencies
     * @return the set of feature dependencies
     */
    Set<String> getDependencies();

    /**
     * Gets true if feature loading has been failed
     * @return {@code true} if feature loading has been failed, {@code false} otherwise
     */
    boolean isFailed();

    /**
     * Gets feature group ID
     * @return the feature group ID
     */
    String getGroupId();

    /**
     * Gets feature version
     * @return the feature version
     */
    String getVersion();

    /**
     * Gets feature location
     * @return the feature location
     */
    IPath getFeatureLocation();

    /**
     * Sets feature name
     * @param featureName the feature name
     */
    void setName(String featureName);

    /**
     * Sets feature dependencies
     * @param dependencies the feature dependencies
     */
    void setDependencies(Set<String> dependencies);

    /**
     * Sets true if feature loading has been failed
     * @param failed {@code true} if feature loading has been failed, {@code false} otherwise
     */
    void setFailed(boolean failed);

    /**
     * Sets feature location
     * @param location the feature location
     */
    void setFeatureLocation(IPath location);

    /**
     * Sets feature group ID
     * @param groupId the feature group ID
     */
    void setGroupId(String groupId);

    /**
     * Sets feature version
     * @param version the feature version
     */
    void setVersion(String version);
}
