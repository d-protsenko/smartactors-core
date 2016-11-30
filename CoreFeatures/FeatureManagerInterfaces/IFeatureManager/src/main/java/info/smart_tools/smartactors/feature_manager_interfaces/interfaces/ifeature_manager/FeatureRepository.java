package info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager;

/**
 * Created by sevenbits on 11/29/16.
 */
public class FeatureRepository implements IFeatureRepository {

    private String repositoryId;
    private String repositoryType;
    private String repositoryUrl;

    public FeatureRepository(final String repositoryId, final String repositoryType, final String repositoryUrl) {
        this.repositoryId = repositoryId;
        this.repositoryType = repositoryType;
        this.repositoryUrl = repositoryUrl;
    }

    @Override
    public String getRepositoryId() {
        return this.repositoryId;
    }

    @Override
    public String getRepositoryType() {
        return this.repositoryType;
    }

    @Override
    public String getRepositoryUrl() {
        return this.repositoryUrl;
    }
}
