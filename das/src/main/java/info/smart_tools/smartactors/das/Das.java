package info.smart_tools.smartactors.das;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.internal.HelpScreenException;

import java.util.HashMap;
import java.util.Map;

public class Das {

    public static void main(final String[] args)
            throws Exception {

        Map<String, IAction> commands = initCommands();

        System.out.println("Distributed Actor System. Design, assembly and deploy tools.");
        System.out.println("Version 0.3.1.");

        if (args.length == 0) {
            return;
        }
        CommandLineArgsResolver clar = null;
        try {
            clar = new CommandLineArgsResolver(args);
        } catch (HelpScreenException e) {
            System.out.println();
            return;
        } catch (ArgumentParserException e) {
            System.out.println("Could not parse commandline arguments: " + e);
            CommandLineArgsResolver.printHelp();
            return;
        }
        if (!clar.isCommand()) {
            CommandLineArgsResolver.printHelp();
            return;
        }
        String command = clar.getCommand();
        if (null == command) {
            CommandLineArgsResolver.printHelp();
            return;
        }
        IAction action = commands.get(command);
        if (null == action) {
            System.out.println("Unavailable command.");
            System.out.println("Available commands: ");
            return;
        }
        try {
            action.execute(args);
        } catch (Throwable e) {
            System.out.println("Command execution has been failed with error: ");
            System.out.println(e.getMessage());
            throw new Exception(e);
        }
    }

    private static Map<String, IAction> initCommands() {
        Map<String, IAction> commands = new HashMap<>();

        commands.put("import_project", new ImportProject());
        commands.put("import", new ImportProject());

        commands.put("create_project", new CreateProject());
        commands.put("cp", new CreateProject());

        commands.put("create_feature", new CreateFeature());
        commands.put("cf", new CreateFeature());

        commands.put("create_actor", new CreateActor());
        commands.put("ca", new CreateActor());

        commands.put("create_plugin", new CreatePlugin());
        commands.put("cpl", new CreatePlugin());

        commands.put("add_feature_repository", new AddOrUpdateFeatureUploadRepository());
        commands.put("afr", new AddOrUpdateFeatureUploadRepository());
        commands.put("update_feature_repository", new AddOrUpdateFeatureUploadRepository());
        commands.put("ufr", new AddOrUpdateFeatureUploadRepository());

        commands.put("add_actor_repository", new AddOrUpdateActorUploadRepository());
        commands.put("aar", new AddOrUpdateActorUploadRepository());
        commands.put("update_actor_repository", new AddOrUpdateActorUploadRepository());
        commands.put("uar", new AddOrUpdateActorUploadRepository());

        commands.put("add_plugin_repository", new AddOrUpdatePluginUploadRepository());
        commands.put("aplr", new AddOrUpdatePluginUploadRepository());
        commands.put("update_plugin_repository", new AddOrUpdatePluginUploadRepository());
        commands.put("uplr", new AddOrUpdatePluginUploadRepository());

        commands.put("add_on_feature_creation_upload_repository", new AddOnFeatureCreationUploadRepository());
        commands.put("aofcur", new AddOnFeatureCreationUploadRepository());

        commands.put("add_on_actor_creation_upload_repository", new AddOnActorCreationUploadRepository());
        commands.put("aoacur", new AddOnActorCreationUploadRepository());

        commands.put("add_on_plugin_creation_upload_repository", new AddOnPluginCreationUploadRepository());
        commands.put("aoplcur", new AddOnPluginCreationUploadRepository());

        commands.put("update_feature_version", new UpdateFeatureVersion());
        commands.put("ufv", new UpdateFeatureVersion());

        commands.put("update_actor_version", new UpdateActorVersion());
        commands.put("uav", new UpdateActorVersion());

        commands.put("update_plugin_version", new UpdatePluginVersion());
        commands.put("uplv", new UpdatePluginVersion());

        return commands;
    }
}
