package info.smart_tools.smartactors.feature_mager_for_corefeatures_and_features.tasks;

import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.IFeatureManager;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;

/**
 * Created by sevenbits on 11/14/16.
 */
public class LoadFeatureTask implements ITask {

    private IFeatureManager manager;

    public LoadFeatureTask() {

    }

    @Override
    public void execute()
            throws TaskExecutionException {
        manager.onLoadedFeature();
    }
}
