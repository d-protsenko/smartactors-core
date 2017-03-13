package info.smart_tools.smartactors.das;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;

import java.util.List;

public class CreateActor implements IAction {

    @Override
    public void execute(final Object o)
            throws ActionExecuteException, InvalidArgumentException {
        System.out.println("Creating actor ...");

        try {
            String[] args = (String[]) o;
            ProjectResolver pr = new ProjectResolver();
            Project project = pr.resolveProject();
            CommandLineArgsResolver clar = new CommandLineArgsResolver(args);
            String name = clar.getActorName();
            String version = null;
            Feature feature = null;
            String featureName = clar.getFeatureName();
            if (null != featureName) {
                feature = project.getFeatures().stream().filter(f -> f.getName().equals(featureName)).findFirst().orElse(null);
            }
            if (null == feature) {
                feature = pr.getCurrentFeature();
            }
            if (clar.isVersion()) {
                version = clar.getVersion();
            } else {
                version = feature.getVersion();
            }

            Actor actor = new Actor(name, version, feature);

            // Addition the actor directory
            actor.makeActorDirectory();

            // Addition the actor pom.xml file
            actor.makePomFile();

            // Addition upload repositories
            List<UploadRepository> repositories = project.getActorOnCreationUploadRepositories();
            for (UploadRepository repository : repositories) {
                actor.addOrUpdateUploadRepository(repository);
                actor.addOrUpdateUploadRepositoryToPom(repository.getId(), repository.getUrl());
            }

            // Creation dir src/main/java/ ${feat.group.id}/ ${feat.name}/ ${actor.name}
            actor.makeClassDirectory();

            // Creation actor class file in the src/main/java/ ${feat.group.id}/ ${feat.name}/ ${actor.name}
            actor.makeClassFile();

            // Creation dir src/main/java/ ${feat.group.id}/ ${feat.name}/ ${actor.name} / exception
            actor.makeExceptionDirectory();

            // Creation exception template file in the src/main/java/ ${feat.group.id}/ ${feat.name}/ ${actor.name} / exception
            actor.makeExceptionTemplate();

            // Creation dir src/main/java/ ${feat.group.id}/ ${feat.name}/ ${actor.name} / wrapper
            actor.makeWrapperDirectory();

            // Creation wrapper template file in the src/main/java/java/ ${feat.group.id}/ ${feat.name}/ ${actor.name} / wrapper
            actor.makeWrapperTemplate();

            // Creation dir src/test/java/ ${feat.group.id}/ ${feat.name}/ ${actor.name}
            actor.makeTestDirectory();

            // Creation actor test class file in the src/test/java/ ${feat.group.id}/ ${feat.name}/ ${actor.name}Test
            actor.makeTestClass();

            // Addition the module section to the project pom
            actor.updateFeaturePom();

            // Addition actor to the feature
            feature.addOrUpdateActor(actor);

            // Save project meta data file
            actor.getOwnerFeature().getOwnerProject().saveMetaDataFile();

        } catch (Exception e) {
            System.out.println("Actor creation has been failed.");
            System.err.println(e);

            throw new ActionExecuteException(e);
        }
        System.out.println("Actor has been created successful.");
    }
}
