package info.smart_tools.smartactors.ads;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;

import java.util.ArrayList;
import java.util.List;

public class AddOnFeatureCreationUploadRepository implements IAction {

    @Override
    public void execute(Object o)
            throws ActionExecuteException, InvalidArgumentException {
        System.out.println("Adding/updating on feature creation upload repository ...");

        try {
            String[] args = (String[]) o;
            ProjectResolver pr = new ProjectResolver();
            Project project = pr.resolveProject();

            CommandLineArgsResolver clar = new CommandLineArgsResolver(args);
            String id = clar.getUploadRepositoryId();
            String url = clar.getUploadRepositoryUrl();

            project.addOrUpdateFeatureUploadRepository(new UploadRepository(id, url));

            // Save project meta data file
            project.saveMetaDataFile();

        } catch (Exception e) {
            System.out.println("Addition/update repository has been failed.");
            System.err.println(e);

            throw new ActionExecuteException(e);
        }
        System.out.println("Repository has been added/updated successful.");
    }
}
