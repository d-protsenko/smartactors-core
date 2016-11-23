package info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager;

/**
 * Created by sevenbits on 11/21/16.
 */
public class FeatureManagerGlobal {
    private static IFeatureManager featureManager;

    public static IFeatureManager get() {
        return featureManager;
    }

    public static void set(final IFeatureManager manager) {
        featureManager = manager;
    }
}
