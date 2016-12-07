package info.smart_tools.smartactors.feature_management.feature;

import info.smart_tools.smartactors.base.interfaces.ipath.IPath;
import info.smart_tools.smartactors.feature_management.interfaces.ifeature.IFeature;

import java.util.Set;

/**
 * Created by sevenbits on 11/14/16.
 */
public class Feature implements IFeature {

    private String name;
    private Set<String> dependencies;
    private IPath featureLocation;
    private boolean failed;
    private String groupId;
    private String version;

    public Feature(String name, Set<String> dependencies, IPath location) {
        this.name = name;
        this.dependencies = dependencies;
        this.featureLocation = location;
        this.failed = false;
    }

    public Feature(String name, String groupId, String version, IPath featureLocation) {
        this.name = name;
        this.groupId = groupId;
        this.version = version;
        this.featureLocation = featureLocation;
        this.failed = false;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Set<String> getDependencies() {
        return this.dependencies;
    }

    @Override
    public IPath getFeatureLocation() {
        return this.featureLocation;
    }

    @Override
    public boolean isFailed() {
        return this.failed;
    }

    @Override
    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    @Override
    public void setName(String featureName) {
        this.name = featureName;
    }

    @Override
    public void setDependencies(Set<String> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public void setFeatureLocation(IPath location) {
        this.featureLocation = (IPath) location;
    }

    @Override
    public String getGroupId() {
        return this.groupId;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public void setGroupId(String groupId) {
        this.groupId = (String) groupId;
    }

    @Override
    public void setVersion(String version) {
        this.version = (String) version;
    }
}
