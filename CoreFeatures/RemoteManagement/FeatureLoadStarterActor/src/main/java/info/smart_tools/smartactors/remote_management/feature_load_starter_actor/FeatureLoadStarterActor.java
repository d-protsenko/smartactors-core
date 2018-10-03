package info.smart_tools.smartactors.remote_management.feature_load_starter_actor;

import info.smart_tools.smartactors.remote_management.feature_load_starter_actor.exception.FeatureLoadStarterException;
import info.smart_tools.smartactors.remote_management.feature_load_starter_actor.wrapper.SetParamsToLoadFromFileWrapper;

import java.io.File;

public class FeatureLoadStarterActor {

    /**
     * Setup parameters to start loading feature(s) from file using 'onNewFile' system chain
     * @param wrapper the wrapped message for getting needed data and storing result
     * @throws FeatureLoadStarterException if any errors occurred on feature creation
     */
    public void setParametersForLoadFromFile(final SetParamsToLoadFromFileWrapper wrapper)
            throws FeatureLoadStarterException {
        try {
            String location = wrapper.getFeatureLocation();
            File f = new File(location);
            wrapper.setFileName(f.getName());
            wrapper.setObservedDirectory(f.getParent());

        } catch(Exception e) {
            throw new FeatureLoadStarterException(e);
        }
    }

}
