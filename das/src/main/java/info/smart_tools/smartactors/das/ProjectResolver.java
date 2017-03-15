package info.smart_tools.smartactors.das;


import java.nio.file.Path;
import java.nio.file.Paths;

public class ProjectResolver {

    private Project project = null;
    private Feature currentFeature = null;

    private final static String PROJECT_META_DATA_FILE = "das.data";

    public Project resolveProject()
            throws Exception {
        Path projectPath = Paths.get("");
        Path path = Paths.get("", PROJECT_META_DATA_FILE);
        if (path.toFile().exists()) {
            this.project = new Project(JsonFile.load(path.toFile()), projectPath);

            return this.project;
        }

        projectPath = Paths.get(Paths.get("").toAbsolutePath().getParent().toString());
        path = Paths.get(Paths.get("").toAbsolutePath().getParent().toString(), PROJECT_META_DATA_FILE);
        if (path.toFile().exists()) {
            this.project = new Project(JsonFile.load(path.toFile()), projectPath);

            Path featurePath = Paths.get("").toAbsolutePath();
//            Path projectPath = Paths.get(featurePath.toAbsolutePath().toString());
            String featureName = featurePath.relativize(projectPath).toString();
            this.currentFeature = this.project.getFeatures().stream()
                    .filter(f -> f.getName().equals(featureName))
                    .findFirst()
                    .orElse(null);

            return this.project;
        }

        System.out.println("Could not find project meta data. Please enter to the project dir (feature dir) and try again.");
        throw new Exception("ProjectResolver:resolveProject - failed. ");
    }

    public Project createProject(final String name, final String groupId, final String version)
            throws Exception {
        this.project = new Project(name, groupId, version, Paths.get(""));

        return this.project;
    }

    public Feature getCurrentFeature() {
        return this.currentFeature;
    }
}
