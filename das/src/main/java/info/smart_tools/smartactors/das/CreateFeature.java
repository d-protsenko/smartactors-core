package info.smart_tools.smartactors.das;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;

import java.util.List;

public class CreateFeature implements IAction {

    @Override
    public void execute(final Object o)
            throws ActionExecuteException, InvalidArgumentException {
        System.out.println("Creating feature ...");

        try {
            String[] args = (String[]) o;
            ProjectResolver pr = new ProjectResolver();
            Project project = pr.resolveProject();
            CommandLineArgsResolver clar = new CommandLineArgsResolver(args);
            String name = clar.getFeatureName();
            String groupId = null;
            if (clar.isGroupId()) {
                groupId = clar.getGroupId();
            } else {
                groupId = project.getGroupId();
            }
            String version = null;
            if (clar.isVersion()) {
                version = clar.getVersion();
            } else {
                version = project.getVersion();
            }

            Feature feature = new Feature(name, groupId, version, project);

            // Addition the feature directory
            feature.makeFeatureDirectory();

            // Addition the feature pom.xml file
            feature.makePomFile();

            // Addition upload repositories
            List<UploadRepository> repositories = project.getFeatureOnCreationUploadRepositories();
            for (UploadRepository repository : repositories) {
                feature.addOrUpdateUploadRepository(repository);
                feature.addOrUpdateUploadRepositoryToPom(repository.getId(), repository.getUrl());
            }

            // Addition the feature config.json file
            feature.makeConfigFile();

            // Creation the distribution module
            feature.makeDistributionModule();

            // Addition the module section to the project pom
            feature.updateProjectPom();

            // Addition feature to the project
            project.addOrUpdateFeature(feature);

            // Update the project meta data file
            feature.getOwnerProject().saveMetaDataFile();


        } catch (Exception e) {
            throw new ActionExecuteException("Could not create instance of Feature.", e);
        }

        System.out.println("Feature has been created successful.");
    }
}
