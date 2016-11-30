package info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sevenbits on 11/21/16.
 */
public class FeatureManagerGlobal {
    private static IFeatureManager featureManager;
    private static final Map<String, IFeatureRepository> repositoryStorage = new HashMap<>();

    public static IFeatureManager get() {
        return featureManager;
    }

    public static void set(final IFeatureManager manager) {
        featureManager = manager;
    }

    public static void addRepository(IFeatureRepository repository) {
        repositoryStorage.put(repository.getRepositoryId(), repository);
    }

    public static Collection<IFeatureRepository> getRepositories () {
        return repositoryStorage.values();
    }
}
