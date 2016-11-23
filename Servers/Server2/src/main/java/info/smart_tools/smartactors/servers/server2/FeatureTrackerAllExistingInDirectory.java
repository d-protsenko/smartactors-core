package info.smart_tools.smartactors.servers.server2;

import info.smart_tools.smartactors.base.interfaces.ipath.IPath;
import info.smart_tools.smartactors.base.path.Path;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.Feature;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.IFeatureManager;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.exception.FeatureManagementException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * Created by sevenbits on 11/21/16.
 */
public class FeatureTrackerAllExistingInDirectory implements IFeatureTracker {

    private IFeatureManager featureManager;
    private IPath path;

    public FeatureTrackerAllExistingInDirectory(IFeatureManager featureManager, IPath path) {
        this.featureManager = featureManager;
        this.path = path;
    }

    @Override
    public void start() {
        try {
            File[] f = new File(this.path.getPath()).listFiles(File::isDirectory);
            Set<IFeature> features = Arrays.stream(f).map(m -> {
                        try {
                            return createFeature(m);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

            ).collect(toSet());
            featureManager.addFeatures(features);
            File[] fZipped = new File(this.path.getPath()).listFiles((item, string) ->  string.endsWith(".zip"));
            features.addAll(Arrays.stream(fZipped).map(m -> {
                        try {
                            return createZippedFeature(m);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

            ).collect(toSet()));
        } catch (FeatureManagementException e) {

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
        return new Feature(f.getName(), null, null);
    }
}

