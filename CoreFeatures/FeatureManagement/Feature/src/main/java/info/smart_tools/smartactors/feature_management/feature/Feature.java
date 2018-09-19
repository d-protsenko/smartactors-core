package info.smart_tools.smartactors.feature_management.feature;

import info.smart_tools.smartactors.base.interfaces.ipath.IPath;
import info.smart_tools.smartactors.feature_management.interfaces.ifeature.IFeature;

import java.util.Set;

/**
 * Implementation of {@link IFeature}
 */
public class Feature implements IFeature {

    private String name;
    private Set<String> dependencies;
    private IPath featureLocation;
    private boolean failed;
    private String groupId;
    private String version;
    private Object id;
    private String packageType;

    private final static String FEATURE_NAME_DELIMITER = ":";

    /**
     * Creates instance of {@link IFeature} by specific arguments
     * @param name the feature name
     * @param groupId the feature group id
     * @param version the feature version
     * @param featureLocation the feature location
     */
    public Feature(
            final String name,
            final String groupId,
            final String version,
            final Set<String> dependencies,
            final IPath featureLocation,
            final String packageType
    ) {
        this.name = name;
        this.groupId = groupId;
        this.version = version;
        this.dependencies = dependencies;
        this.featureLocation = featureLocation;
        this.failed = false;
        this.id = java.util.UUID.randomUUID();
        this.packageType = packageType;
    }

    @Override
    public String getName() { return this.name; }

    @Override
    public String getGroupId() {
        return this.groupId;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public Set<String> getDependencies() { return this.dependencies; }

    @Override
    public IPath getLocation() { return this.featureLocation; }

    @Override
    public Object getId() { return this.id; }

    @Override
    public String getPackageType() {
        return this.packageType;
    }

    @Override
    public boolean isFailed() { return this.failed; }

    @Override
    public void setFailed(final boolean failed) { this.failed = failed; }

    @Override
    public void setName(final String featureName) { this.name = featureName;}

    @Override
    public void setDependencies(final Set<String> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public void setLocation(final IPath location) {
        this.featureLocation = (IPath) location;
    }

    @Override
    public void setGroupId(final String groupId) {
        this.groupId = groupId;
    }

    @Override
    public void setVersion(final String version) {
        this.version = version;
    }

    @Override
    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }
}
