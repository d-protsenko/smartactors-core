package info.smart_tools.smartactors.ads;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;

public class CreateProject implements IAction {

    @Override
    public void execute(final Object o)
            throws ActionExecuteException, InvalidArgumentException {
        System.out.println("Creating project ...");
        String[] args = (String[]) o;

        try {
            CommandLineArgsResolver clar = new CommandLineArgsResolver(args);
            String name = clar.getProjectName();
            String group = clar.getGroupId();
            String version = clar.getVersion();
            ProjectResolver pr = new ProjectResolver();

            Project project = pr.createProject(name, group, version);

            project.makeProjectDirectory();

            project.makePomFile();

            project.saveMetaDataFile();
        } catch (Exception e) {
            System.out.println("Project creation has been failed.");
            System.err.println(e);

            throw new ActionExecuteException(e);
        }
        System.out.println("Project has been created successful.");
    }
}
