package info.smart_tools.smartactors.ads;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class CommandLineArgsResolver {

    private static ArgumentParser parser = ArgumentParsers.newArgumentParser("ads");

    private Namespace ns = null;

    static {
        parser.addArgument("-c", "--command")
                .required(true)
                .choices("import", "cp", "cf", "ca", "cpl", "afr",
                        "ufr", "aar", "uar", "aplr", "uplr", "aofcur",
                        "aoacur", "aoplcur", "ufv", "uav", "uplv"
                )
                .help("a commands (\n" +
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
    }

    public CommandLineArgsResolver(final String[] args)
            throws Exception {
        try {
            this.ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            System.out.println("Could not parse commandline arguments." + e);
            parser.handleError(e);
//            throw new Exception("Could not parse commandline arguments.");
        }
    }

    public String getProjectName()
            throws Exception {
        String projectName = null;
        projectName = ns.getString("project_name");
        if (null == projectName) {
            System.out.println("Could not resolve argument: -pn | -project_name.");
            throw new Exception("Could not resolve argument: -pn | -project_name.");
        }

        return projectName;
    }

    public String getCommand()
            throws Exception {
        String command = null;
        command = ns.getString("command");
        if (null == command) {
            System.out.println("Could not resolve argument: -c | -command.");
            throw new Exception("Could not resolve argument: -c | -command.");
        }

        return command;
    }

    public boolean isProjectName() {
        String project = ns.getString("project_name");
        return !(null == project || project.isEmpty());
    }

    public String getFeatureName()
            throws Exception {
        String featureName = null;
        featureName = ns.getString("feature_name");
        if (null == featureName) {
            System.out.println("Could not resolve argument: -fn | -feature_name.");
            throw new Exception("Could not resolve argument: -fn | -feature_name.");
        }
        return featureName;
    }

    public boolean isFeatureName() {
        String feature = ns.getString("feature_name");
        return !(null == feature || feature.isEmpty());
    }

    public String getActorName()
            throws Exception {
        String actorName = null;
        actorName = ns.getString("actor_name");
        if (null == actorName) {
            System.out.println("Could not resolve argument: -an | -actor_name.");
            throw new Exception("Could not resolve argument: -an | -actor_name.");
        }

        return actorName;
    }

    public boolean isActorName() {
        String actor = ns.getString("actor_name");
        return !(null == actor || actor.isEmpty());
    }


    public String getPluginName()
            throws Exception {
        String pluginName = null;
        pluginName = ns.getString("plugin_name");
        if (null == pluginName) {
            System.out.println("Could not resolve argument: -pln | -plugin_name.");
            throw new Exception("Could not resolve argument: -pln | -plugin_name.");
        }

        return pluginName;
    }

    public boolean isPluginName() {
        String plugin = ns.getString("plugin_name");
        return !(null == plugin || plugin.isEmpty());
    }

    public String getGroupId()
            throws Exception {
        String groupId = null;
        groupId = ns.getString("group_id");
        if (null == groupId) {
            System.out.println("Could not resolve argument: -group_id | -g.");
            throw new Exception("Could not resolve argument: -group_id | -g.");
        }

        return groupId;
    }

    public boolean isGroupId() {
        String groupId = ns.getString("group_id");
        return !(null == groupId || groupId.isEmpty());
    }

    public String getVersion()
            throws Exception {
        String version = null;
        version = ns.getString("version");
        if (null == version) {
            System.out.println("Could not resolve argument: -version | -v.");
            throw new Exception("Could not resolve argument: -version | -v.");
        }

        return version;
    }

    public boolean isVersion() {
        String version = ns.getString("version");
        return !(null == version || version.isEmpty());
    }

    public String getUploadRepositoryId()
            throws Exception {
        String repositoryId = null;
        repositoryId = ns.getString("repository_id");
        if (null == repositoryId) {
            System.out.println("Could not resolve argument: -repository_id | -r_id | -rid | -id.");
            throw new Exception("Could not resolve argument: -repository_id | -r_id | -rid | -id.");
        }

        return repositoryId;
    }

    public boolean isUploadRepositoryId() {
        String repositoryId = ns.getString("repository_id");
        return !(null == repositoryId || repositoryId.isEmpty());
    }

    public String getUploadRepositoryUrl()
            throws Exception {
        String repositoryUrl = null;
        repositoryUrl = ns.getString("repository_url");
        if (null == repositoryUrl) {
            System.out.println("Could not resolve argument: -repository_url | -r_url | -rurl | -url.");
            throw new Exception("Could not resolve argument: -repository_url | -r_url | -rurl | -url.");
        }

        return repositoryUrl;
    }

    public boolean isUploadRepositoryUrl() {
        String repositoryUrl = ns.getString("repository_url");
        return !(null == repositoryUrl || repositoryUrl.isEmpty());
    }

    public String getPath()
            throws Exception {
        String path = null;
        path = ns.getString("path");
        if (null == path) {
            System.out.println("Could not resolve argument: -path.");
            throw new Exception("Could not resolve argument: -path.");
        }

        return path;
    }

    public boolean isPath() {
        String path = ns.getString("path");
        return !(null == path || path.isEmpty());
    }
}
