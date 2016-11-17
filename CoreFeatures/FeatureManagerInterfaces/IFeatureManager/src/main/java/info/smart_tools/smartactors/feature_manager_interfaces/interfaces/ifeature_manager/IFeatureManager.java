package info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.exception.FeatureManagementException;

import java.util.Collection;
import java.util.Set;

/**
 * Created by sevenbits on 11/14/16.
 */
public interface IFeatureManager {

    void addFeatures(Set<IFeature> features) throws FeatureManagementException;

    void onCompleteFeatureOperation(IFeature feature) throws FeatureManagementException;

    Collection<IFeature> getLoadedFeatures();

    Collection<IFeature> getFailedFeatures();
}
