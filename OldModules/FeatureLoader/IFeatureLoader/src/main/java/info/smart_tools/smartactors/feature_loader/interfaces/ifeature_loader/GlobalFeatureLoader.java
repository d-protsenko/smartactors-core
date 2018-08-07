package info.smart_tools.smartactors.feature_loader.interfaces.ifeature_loader;

/**
 *
 */
public class GlobalFeatureLoader {
    private static IFeatureLoader featureLoader;

    /**
     * Get the global feature loader.
     *
     * @return the global feature loader
     */
    public static IFeatureLoader get() {
        return featureLoader;
    }

    /**
     * Set new global feature loader.
     *
     * @param theFeatureLoader    the new feature loader to use as global
     */
    public static void set(final IFeatureLoader theFeatureLoader) {
        featureLoader = theFeatureLoader;
    }
}
