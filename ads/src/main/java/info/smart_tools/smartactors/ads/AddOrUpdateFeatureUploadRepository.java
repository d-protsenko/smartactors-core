package info.smart_tools.smartactors.ads;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;

import java.util.ArrayList;
import java.util.List;

public class AddOrUpdateFeatureUploadRepository implements IAction {

    @Override
    public void execute(Object o) throws ActionExecuteException, InvalidArgumentException {
        System.out.println("Adding/updating repository to the feature ...");

        try {
            String[] args = (String[]) o;
            ProjectResolver pr = new ProjectResolver();
            Project project = pr.resolveProject();
            Feature feature = null;
            CommandLineArgsResolver clar = new CommandLineArgsResolver(args);
            String featureName = clar.getFeatureName();
            List<Feature> features = new ArrayList<>();
            if (null != featureName) {
                feature = project.getFeatures().stream().filter(f -> f.getName().equals(featureName)).findFirst().orElse(null);
            }
            if (null == feature) {
                feature = pr.getCurrentFeature();
            }
            if (null != feature) {
                features.add(feature);
            }
            if (null == featureName || featureName.toLowerCase().equals("all")) {
                features = project.getFeatures();
            }
            String repId = clar.getUploadRepositoryId();
            String repUrl = clar.getUploadRepositoryUrl();

            for(Feature f : features) {
                System.out.println("Feature - " + f.getName() + " ...");
                forOneFeature(f, repId, repUrl);
                System.out.println("... processed.");
            }

            // Save project meta data file
            project.saveMetaDataFile();

        } catch (Exception e) {
            System.out.println("Addition/update repository has been failed.");
            System.err.println(e);

            throw new ActionExecuteException(e);
        }
        System.out.println("Repository has been added/updated successful.");
    }

    private void forOneFeature(final Feature feature, final String repId, final String repUrl)
            throws Exception {
        // Addition or update feature repository to pom file
        feature.addOrUpdateUploadRepositoryToPom(repId, repUrl);

        // Addition or update feature repository to meta data
        feature.addOrUpdateUploadRepository(new UploadRepository(repId, repUrl));

    }
}
