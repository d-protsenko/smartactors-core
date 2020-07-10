package info.smart_tools.smartactors.remote_management.feature_load_starter_actor;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.remote_management.feature_load_starter_actor.exception.FeatureLoadStarterException;
import info.smart_tools.smartactors.remote_management.feature_load_starter_actor.wrapper.SetParamsToLoadFromFileWrapper;
import info.smart_tools.smartactors.remote_management.feature_load_starter_actor.wrapper.SetParamsToLoadFromRepositoryWrapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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
            String destinationDirectory = wrapper.getFeatureDestinationPath();
            String fileName = f.getName();
            File destination = Paths.get(destinationDirectory, fileName).toFile();
            Files.copy(f.toPath(), destination.toPath());
            wrapper.setFileName(fileName);
            wrapper.setObservedDirectory(destinationDirectory);
        } catch(Exception e) {
            throw new FeatureLoadStarterException(e);
        }
    }

    /**
     * Setup parameters to start loading feature(s) from file using 'onNewFile' system chain
     * @param wrapper the wrapped message for getting needed data and storing result
     * @throws FeatureLoadStarterException if any errors occurred on feature creation
     */
    public void setParametersForLoadFromRepository(final SetParamsToLoadFromRepositoryWrapper wrapper)
            throws FeatureLoadStarterException {
        try {
            List<IObject> repositories = wrapper.getRepositoriesDescription();
            List<IObject> features = wrapper.getFeaturesDescription();

            wrapper.setRepositoriesDescription(repositories);
            wrapper.setFeaturesDescription(features);

        } catch(Exception e) {
            throw new FeatureLoadStarterException(e);
        }
    }

}
