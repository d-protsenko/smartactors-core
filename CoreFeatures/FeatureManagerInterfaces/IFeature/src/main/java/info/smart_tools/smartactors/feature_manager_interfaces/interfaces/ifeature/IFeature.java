package info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature;

import java.util.Set;

/**
 * Created by sevenbits on 11/14/16.
 */
public interface IFeature<T, S> {

    T getName();

    Set<T> getDependencies();

    S getStatus();

    void setStatus(S status);

    <P> P getFeatureLocation();

    void setName(T featureName);

    void setDependencies(Set<T> dependencies);

    <P> void setFeatureLocation(P location);
}
