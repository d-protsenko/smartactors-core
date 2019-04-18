package info.smart_tools.smartactors.das.utilities;

import info.smart_tools.smartactors.das.utilities.exception.InvalidCommandLineArgumentException;
import info.smart_tools.smartactors.das.utilities.interfaces.ICommandLineArgsResolver;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.internal.HelpScreenException;

public class CommandLineArgsResolver implements ICommandLineArgsResolver {

    private static ArgumentParser parser = ArgumentParsers.newArgumentParser("das");

    private Namespace ns = null;

    static {
        parser.addArgument("command")
                .choices(
                        "make", "deploy", "import", "cp", "cf", "ca", "cpl", "afr",
                        "ufr", "aar", "uar", "aplr", "uplr", "aofcur",
                        "aoacur", "aoplcur", "ufv", "uav", "uplv", "cs", "dc"
                )
                .help("a commands (\n" +
                        "\t make - compile actors/plugins and aggregate them to feature zip files\n" +
                        "\t deploy - deploy compiled actors/plugins and aggregated features to upload repositories." +
                        "\t cs - create a server with [-v VERSION] version into the [-path PATH] directory" +
                        "\t dc - download and unzip a core pack for server in accordance with [-path PATH] core.json file" +
                        "\t import - import the project from [-path PATH] to a new folder with name [-pn PROJECT_NAME]\n" +
                        "\t cp - create a new project [-pn PROJECT_NAME] [-g GROUP_ID] [-v VERSION]\n" +
                        "\t cf - create a new feature [-fn FEATURE_NAME] [-g GROUP_ID] [-v VERSION]\n" +
                        "\t ca - create a new actor [-an ACTOR_NAME]\n" +
                        "\t cpl - create a new plugin [-pln PLUGIN_NAME]\n" +
                        "\t afr - add or update upload repository [-rid REPOSITORY_ID] [-rurl REPOSITORY_URL] to the feature with name [-fn FEATURE_NAME | all]\n" +
                        "\t afr - add or update upload repository [-rid REPOSITORY_ID] [-rurl REPOSITORY_URL] to the feature with name [-fn FEATURE_NAME | all]\n" +
                        "\t aar - add or update upload repository [-rid REPOSITORY_ID] [-rurl REPOSITORY_URL] to the actor with name [-an ACTOR_NAME | all] of feature with name [-fn FEATURE_NAME | all]\n" +
                        "\t uar - add or update upload repository [-rid REPOSITORY_ID] [-rurl REPOSITORY_URL] to the actor with name [-an ACTOR_NAME | all] of feature with name [-fn FEATURE_NAME | all]\n" +
                        "\t aplr - add or update upload repository [-rid REPOSITORY_ID] [-rurl REPOSITORY_URL] to the plugin with name [-pln ACTOR_NAME | all] of feature with name [-fn FEATURE_NAME | all]\n\n" +
                        "\t aplr - add or update upload repository [-rid REPOSITORY_ID] [-rurl REPOSITORY_URL] to the plugin with name [-pln ACTOR_NAME | all] of feature with name [-fn FEATURE_NAME | all]\n\n" +
                        "\t aofcur - add or update upload repository [-rid REPOSITORY_ID] [-rurl REPOSITORY_URL] to the feature on feature creation\n" +
                        "\t aoacur - add or update upload repository [-rid REPOSITORY_ID] [-rurl REPOSITORY_URL] to the actor on actor creation\n" +
                        "\t aoplcur - add or update upload repository [-rid REPOSITORY_ID] [-rurl REPOSITORY_URL] to the plugin on plugin creation\n" +
                        "\t ufv - update version [-v] of feature with name [-fn FEATURE_NAME | all]\n" +
                        "\t uav - update version [-v] of actor with name [-an ACTOR_NAME | all]\n" +
                        "\t uplv - update version [-v] of plugin with name [-pln PLUGIN_NAME | all]\n" +
                        ")"
                );

        parser.addArgument("-pn", "--project_name").help("the name of project");
        parser.addArgument("-fn", "--feature_name").help("the name of feature");
        parser.addArgument("-an", "--actor_name").help("the name of actor");
        parser.addArgument("-pln", "--plugin_name").help("the name of plugin.");
        parser.addArgument("-v", "--version").help("the version");
        parser.addArgument("-g", "--group_id").help("the group id");
        parser.addArgument("-rid", "--repository_id").help("the repository id");
        parser.addArgument("-rurl", "--repository_url").help("the repository url");
        parser.addArgument("-path", "--path").help("the path");
        parser.addArgument("-sn", "--server_name").help("the name of directory for server (default - 'server')");
        parser.addArgument("-sl", "--source_location").help("the location of source file");
        parser.addArgument("-aid", "--artifact_id").help("the artifact id");
        parser.addArgument("-type", "--type").help("the package type(jar or zip)");
//        parser.addArgument("make")
//                .help("make current project/feature/actor. Will be run follows maven command: mvn clean package");
//        parser.addArgument("deploy")
//                .help("deploy current features/actors to the upload server(s). Will be run follows maven command: mvn -Dmaven.test.skip=true -DdeployOnly=true deploy");
    }

    public CommandLineArgsResolver(final String[] args)
            throws ArgumentParserException {
            this.ns = parser.parseArgs(args);
    }

    public String getProjectName()
            throws InvalidCommandLineArgumentException {
        String projectName = null;
        projectName = ns.getString("project_name");
        if (null == projectName) {
            throw new InvalidCommandLineArgumentException("Could not resolve argument: -pn | -project_name.");
        }

        return projectName;
    }

    public boolean isProjectName() {
        String project = ns.getString("project_name");
        return !(null == project || project.isEmpty());
    }

    public String getCommand()
            throws InvalidCommandLineArgumentException {
        String command = null;
        command = ns.getString("command");
        if (null == command) {
            throw new InvalidCommandLineArgumentException("Could not resolve argument: command.");
        }

        return command;
    }

    public boolean isCommand() {
        String command = ns.getString("command");
        return !(null == command || command.isEmpty());
    }


    public String getFeatureName()
            throws InvalidCommandLineArgumentException {
        String featureName = null;
        featureName = ns.getString("feature_name");
        if (null == featureName) {
            throw new InvalidCommandLineArgumentException("Could not resolve argument: -fn | -feature_name.");
        }
        return featureName;
    }

    public boolean isFeatureName() {
        String feature = ns.getString("feature_name");
        return !(null == feature || feature.isEmpty());
    }

    public String getActorName()
            throws InvalidCommandLineArgumentException {
        String actorName = null;
        actorName = ns.getString("actor_name");
        if (null == actorName) {
            throw new InvalidCommandLineArgumentException("Could not resolve argument: -an | -actor_name.");
        }

        return actorName;
    }

    public boolean isActorName() {
        String actor = ns.getString("actor_name");
        return !(null == actor || actor.isEmpty());
    }


    public String getPluginName()
            throws InvalidCommandLineArgumentException {
        String pluginName = null;
        pluginName = ns.getString("plugin_name");
        if (null == pluginName) {
            throw new InvalidCommandLineArgumentException("Could not resolve argument: -pln | -plugin_name.");
        }

        return pluginName;
    }

    public boolean isPluginName() {
        String plugin = ns.getString("plugin_name");
        return !(null == plugin || plugin.isEmpty());
    }

    public String getGroupId()
            throws InvalidCommandLineArgumentException {
        String groupId = null;
        groupId = ns.getString("group_id");
        if (null == groupId) {
            throw new InvalidCommandLineArgumentException("Could not resolve argument: -g | -group_id.");
        }

        return groupId;
    }

    public boolean isGroupId() {
        String groupId = ns.getString("group_id");
        return !(null == groupId || groupId.isEmpty());
    }

    public String getVersion()
            throws InvalidCommandLineArgumentException {
        String version = null;
        version = ns.getString("version");
        if (null == version) {
            throw new InvalidCommandLineArgumentException("Could not resolve argument: -v | -version.");
        }

        return version;
    }

    public boolean isVersion() {
        String version = ns.getString("version");
        return !(null == version || version.isEmpty());
    }

    public String getUploadRepositoryId()
            throws InvalidCommandLineArgumentException {
        String repositoryId = null;
        repositoryId = ns.getString("repository_id");
        if (null == repositoryId) {
            throw new InvalidCommandLineArgumentException("Could not resolve argument: -rid | -repository_id.");
        }

        return repositoryId;
    }

    public boolean isUploadRepositoryId() {
        String repositoryId = ns.getString("repository_id");
        return !(null == repositoryId || repositoryId.isEmpty());
    }

    public String getUploadRepositoryUrl()
            throws InvalidCommandLineArgumentException {
        String repositoryUrl = null;
        repositoryUrl = ns.getString("repository_url");
        if (null == repositoryUrl) {
            throw new InvalidCommandLineArgumentException("Could not resolve argument: -rurl | -repository_url.");
        }

        return repositoryUrl;
    }

    public boolean isUploadRepositoryUrl() {
        String repositoryUrl = ns.getString("repository_url");
        return !(null == repositoryUrl || repositoryUrl.isEmpty());
    }

    public String getServerDirectory()
            throws InvalidCommandLineArgumentException {
        String serverDirectory = null;
        serverDirectory = ns.getString("server_name");
        if (null == serverDirectory) {
            throw new InvalidCommandLineArgumentException("Could not resolve argument: -sn | -server_name.");
        }

        return serverDirectory;
    }

    public boolean isServerName() {
        String serverDirectory = ns.getString("server_name");
        return !(null == serverDirectory || serverDirectory.isEmpty());
    }

    public String getSourceLocation()
            throws InvalidCommandLineArgumentException {
        String sourceLocation = null;
        sourceLocation = ns.getString("source_location");
        if (null == sourceLocation) {
            throw new InvalidCommandLineArgumentException("Could not resolve argument: -sl | -source_location.");
        }

        return sourceLocation;
    }

    public boolean isSourceLocation() {
        String sourceLocation = ns.getString("source_location");
        return !(null == sourceLocation || sourceLocation.isEmpty());
    }

    public String getArtifactId()
            throws InvalidCommandLineArgumentException {
        String artifactId = null;
        artifactId = ns.getString("artifact_id");
        if (null == artifactId) {
            throw new InvalidCommandLineArgumentException("Could not resolve argument: -aid | -artifact_id.");
        }

        return artifactId;
    }

    public boolean isArtifactId() {
        String artifactId = ns.getString("artifact_id");
        return !(null == artifactId || artifactId.isEmpty());
    }

    public String getPath()
            throws InvalidCommandLineArgumentException {
        String path = null;
        path = ns.getString("path");
        if (null == path) {
            throw new InvalidCommandLineArgumentException("Could not resolve argument: -path.");
        }

        return path;
    }

    public boolean isPath() {
        String path = ns.getString("path");
        return !(null == path || path.isEmpty());
    }

    public String getPackageType()
            throws InvalidCommandLineArgumentException {
        String type = null;
        type = ns.getString("type");
        if (null == type) {
            throw new InvalidCommandLineArgumentException("Could not resolve argument: -type.");
        }

        return type;
    }

    public boolean isPackageType() {
        String type = ns.getString("type");
        return !(null == type || type.isEmpty());
    }

    public boolean isHelp() {
        return this.ns == null;
    }

    public void printHelp() {
        parser.printHelp();
    }
}
