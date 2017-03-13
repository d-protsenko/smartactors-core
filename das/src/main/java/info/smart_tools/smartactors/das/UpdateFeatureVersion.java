package info.smart_tools.smartactors.das;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;

import java.util.ArrayList;
import java.util.List;

public class UpdateFeatureVersion implements IAction {

    @Override
    public void execute(Object o)
            throws ActionExecuteException, InvalidArgumentException {
        System.out.println("Updating feature version ...");

        try {
            String[] args = (String[]) o;
            ProjectResolver pr = new ProjectResolver();
            Project project = pr.resolveProject();

            CommandLineArgsResolver clar = new CommandLineArgsResolver(args);
            String newVersion = clar.getVersion();
            Feature feature = null;
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

            for(Feature f : features) {
                System.out.println("Feature - " + f.getName() + " ...");
                f.setVersion(newVersion);
                f.updateVersionInPom(newVersion);
                System.out.println("... processed.");
            }

            // Save project meta data file
            project.saveMetaDataFile();

        } catch (Exception e) {
            System.out.println("Update feature version has been failed.");
            System.err.println(e);

            throw new ActionExecuteException(e);
        }
        System.out.println("Feature version has been updated successful.");
    }
}
