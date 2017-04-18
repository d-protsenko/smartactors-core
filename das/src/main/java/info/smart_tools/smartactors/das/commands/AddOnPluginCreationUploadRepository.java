package info.smart_tools.smartactors.das.commands;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.das.utilities.CommandLineArgsResolver;
import info.smart_tools.smartactors.das.utilities.ProjectResolver;
import info.smart_tools.smartactors.das.models.Project;
import info.smart_tools.smartactors.das.models.UploadRepository;
import info.smart_tools.smartactors.das.utilities.exception.InvalidCommandLineArgumentException;
import info.smart_tools.smartactors.das.utilities.exception.ProjectResolutionException;
import info.smart_tools.smartactors.das.utilities.interfaces.ICommandLineArgsResolver;
import info.smart_tools.smartactors.das.utilities.interfaces.IProjectResolver;

public class AddOnPluginCreationUploadRepository implements IAction {

    @Override
    public void execute(Object o)
            throws ActionExecuteException, InvalidArgumentException {
        System.out.println("Adding/updating on feature creation upload repository ...");

        try {
            ICommandLineArgsResolver clar = (ICommandLineArgsResolver) ((Object[])o)[0];
            IProjectResolver pr = (IProjectResolver)  ((Object[])o)[1];
            Project project = pr.resolveProject();

            String id = clar.getUploadRepositoryId();
            String url = clar.getUploadRepositoryUrl();
            project.addOrUpdatePluginUploadRepository(new UploadRepository(id, url));

            // Save project meta data file
            project.saveMetaDataFile();

        } catch (InvalidCommandLineArgumentException | ProjectResolutionException e) {
            System.out.println(e.getMessage());

            return;
        } catch (Exception e) {
            System.out.println("Addition/update repository has been failed.");
            System.err.println(e);

            throw new ActionExecuteException(e);
        }
        System.out.println("Repository has been added/updated successful.");
    }
}
