package info.smart_tools.smartactors.feature_mager_for_corefeatures_and_features.feature_manager;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.IFeatureManager;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sevenbits on 11/14/16.
 */
public class FeatureManager implements IFeatureManager {

    private Map<String, IFeature> loadedFeatures;
    private Map<String, IFeature> failedFeatures;
    private IQueue queue;


    public FeatureManager(IQueue queue) {
        this.loadedFeatures = new HashMap<>();
        this.failedFeatures = new HashMap<>();
        this.queue = queue;
    }

    @Override
    public void loadFeature() {
        ITask task =
    }

    @Override
    public void onLoadedFeature(IAction<IFeature> iAction) {

    }

    @Override
    public List<IFeature> getLoadedFeatures() {
        return null;
    }

    @Override
    public List<IFeature> getFailedFeatures() {
        return null;
    }
}
