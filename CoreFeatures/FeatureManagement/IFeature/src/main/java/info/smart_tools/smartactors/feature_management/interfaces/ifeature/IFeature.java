package info.smart_tools.smartactors.feature_management.interfaces.ifeature;

import info.smart_tools.smartactors.base.interfaces.ipath.IPath;

import java.util.Set;

/**
 * Created by sevenbits on 11/14/16.
 */
public interface IFeature {

    String getName();

    Set<String> getDependencies();

    boolean isFailed();

    String getGroupId();

    String getVersion();

    IPath getFeatureLocation();

    void setName(String featureName);

    void setDependencies(Set<String> dependencies);

    void setFailed(boolean failed);

    void setFeatureLocation(IPath location);

    void setGroupId(String groupId);

    void setVersion(String version);
}
