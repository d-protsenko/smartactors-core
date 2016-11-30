package info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature;

import java.util.Set;

/**
 * Created by sevenbits on 11/14/16.
 */
public interface IFeature<T, S> {

    T getName();

    Set<T> getDependencies();

    S getStatus();

    <S1> S1 getGroupId();

    <S2> S2 getVersion();

    void setStatus(S status);

    <P> P getFeatureLocation();

    void setName(T featureName);

    void setDependencies(Set<T> dependencies);

    <P> void setFeatureLocation(P location);

    <S1> void setGroupId(S1 groupId);

    <S2> void setVersion(S2 version);
}
