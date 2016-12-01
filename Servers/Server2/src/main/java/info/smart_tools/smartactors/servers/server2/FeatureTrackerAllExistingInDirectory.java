package info.smart_tools.smartactors.servers.server2;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.ipath.IPath;
import info.smart_tools.smartactors.base.path.Path;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.Feature;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.FeatureManager;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.FeatureManagerGlobal;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.FeatureRepository;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.IFeatureManager;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.exception.FeatureManagementException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

/**
 * Created by sevenbits on 11/21/16.
 */
public class FeatureTrackerAllExistingInDirectory implements IFeatureTracker {

    private IFeatureManager featureManager;
    private IPath path;
    private IAction<IFeatureManager> onFeatureLoadCompleted;

    public FeatureTrackerAllExistingInDirectory(
            final IFeatureManager featureManager, final IPath path, final IAction<IFeatureManager> onFeatureLoadCompleted
    ) {
        this.featureManager = featureManager;
        this.path = path;
        this.onFeatureLoadCompleted = onFeatureLoadCompleted;
    }

    @Override
    public void start() {
        try {
            File[] f = new File(this.path.getPath()).listFiles(File::isDirectory);
            File[] fZipped = new File(this.path.getPath()).listFiles((item, string) ->  string.endsWith(".zip"));

            File downloadList = new File(this.path.getPath() + "/features.json");

            Map<String, IFeature> features = new HashMap<>();

            features.putAll(parseFeatureList(downloadList));

            Arrays.stream(f).map(m -> {
                        try {
                            return createFeature(m);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            ).forEach(i -> features.put((String) i.getName(), i));
            Arrays.stream(fZipped).map(m -> {
                        try {
                            return createZippedFeature(m);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            ).forEach(i -> features.put((String) i.getName(), i));


            featureManager.addFeatures(new HashSet<>(features.values()), this.onFeatureLoadCompleted);
        } catch (Exception e) {

        }
    }

    private IFeature createFeature(File f)
            throws Exception {
        File jsonFile = new File(f.getPath() + "/config.json");
        if (!jsonFile.exists()) {
            throw new Exception("File config.json not found in the folder :" + f.getPath());
        }

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(jsonFile));
        JSONObject jsonObject = (JSONObject) obj;

        String name = (String) jsonObject.get("featureName");
        Set<String> dependencies = new HashSet<>();
        JSONArray listOfDependencies = (JSONArray) jsonObject.get("afterFeatures");
        Iterator<String> iterator = listOfDependencies.iterator();
        while (iterator.hasNext()) {
            dependencies.add(iterator.next());
        }

        return new Feature(name, dependencies, new Path(f.getPath()));
    }

    private IFeature createZippedFeature(File f)
            throws Exception {

        return new Feature(parseNameOfZippedFeature(f), null, new Path(f.getPath()));
    }

    private String parseNameOfZippedFeature(File f) {

        return f.getName().split("-\\d\\.\\d\\.\\d-")[0];
    }

    private Map<String, IFeature> parseFeatureList(File jsonFile)
            throws Exception {
        Map<String, IFeature> features = new HashMap<>();
        if (!jsonFile.exists()) {
            return features;
        }
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(jsonFile));
        JSONObject jsonObject = (JSONObject) obj;

        JSONArray listOfRepositories = (JSONArray) jsonObject.get("repositories");
        Iterator<JSONObject> itR = listOfRepositories.iterator();
        while (itR.hasNext()) {
            JSONObject repoJson = ((JSONObject)itR.next());
            FeatureRepository repo = new FeatureRepository(
                    (String) repoJson.get("repositoryId"), (String) repoJson.get("type"), (String) repoJson.get("url")
            );
            FeatureManagerGlobal.addRepository(repo);
        }

        JSONArray listOfFeatures = (JSONArray) jsonObject.get("features");
        Iterator<JSONObject> itF = listOfFeatures.iterator();

        while (itF.hasNext()) {
            JSONObject featureJson = itF.next();
            String name = (String) featureJson.get("name");
            features.put(name, new Feature(
                            (String) featureJson.get("name"),
                            (String) featureJson.get("group"),
                            (String) featureJson.get("version"),
                            new Path(jsonFile.getParent())
                    )
            );
        }

        return features;
    }
}

