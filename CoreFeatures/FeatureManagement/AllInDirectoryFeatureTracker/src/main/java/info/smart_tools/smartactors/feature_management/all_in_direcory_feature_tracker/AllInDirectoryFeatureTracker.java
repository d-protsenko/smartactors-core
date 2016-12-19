package info.smart_tools.smartactors.feature_management.all_in_direcory_feature_tracker;

import info.smart_tools.smartactors.base.path.Path;
import info.smart_tools.smartactors.feature_management.all_in_direcory_feature_tracker.exception.FeatureTrackerException;
import info.smart_tools.smartactors.feature_management.all_in_direcory_feature_tracker.wrapper.FeatureTrackerWrapper;
import info.smart_tools.smartactors.feature_management.feature.Feature;
import info.smart_tools.smartactors.feature_management.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by sevenbits on 12/7/16.
 */
public class AllInDirectoryFeatureTracker {

    private final IFieldName featureNameFN;
    private final IFieldName afterFeaturesFN;
    private final IFieldName repositoriesFN;
    private final IFieldName featuresFN;
    private final IFieldName nameFN;
    private final IFieldName groupFN;
    private final IFieldName versionFN;

    public AllInDirectoryFeatureTracker()
            throws ResolutionException {
        this.featureNameFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "featureName");
        this.afterFeaturesFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "afterFeatures");
        this.repositoriesFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "repositories");

        this.featuresFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "features");
        this.nameFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "name");
        this.groupFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "group");
        this.versionFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "version");
    }

    public void addFeatures(final FeatureTrackerWrapper wrapper)
            throws FeatureTrackerException {
        try {
            String path = wrapper.getPath();
            File[] f = new File(path).listFiles(File::isDirectory);
            File[] fZipped = new File(path).listFiles((item, string) ->  string.endsWith(".zip"));

            File downloadList = new File(path + "/features.json");

            Map<String, IFeature> features = new HashMap<>();

            features.putAll(this.parseFeatureList(downloadList));

            Arrays.stream(f).map(m -> {
                        try {
                            return createFeature(m);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            ).forEach(i -> features.put(i.getName(), i));
            Arrays.stream(fZipped).map(m -> {
                        try {
                            return createZippedFeature(m);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            ).forEach(i -> features.put(i.getName(), i));

            wrapper.setFeatures(features.values());
        } catch (Throwable e) {
            throw new FeatureTrackerException(e);
        }
    }

    private IFeature createFeature(File f)
            throws Exception {
        File jsonFile = new File(f.getPath() + "/config.json");
        if (!jsonFile.exists()) {
            throw new Exception("File config.json not found in the folder :" + f.getPath());
        }

        IObject jsonConfig = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()), new Scanner(jsonFile).useDelimiter("\\Z").next());

        String name = (String) jsonConfig.getValue(this.featureNameFN);
        Set<String> dependencies = new HashSet<>((List) jsonConfig.getValue(this.afterFeaturesFN));

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

        IObject jsonConfig = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()), new Scanner(jsonFile).useDelimiter("\\Z").next());


        List<IObject> repositories = (List<IObject>) jsonConfig.getValue(this.repositoriesFN);
        List<IObject> repositoryStorage = IOC.resolve(Keys.getOrAdd("feature-repositories"));

        for (IObject repository : repositories) {
            repositoryStorage.add(repository);
        }
        List<IObject> featuresFromJson = (List<IObject>) jsonConfig.getValue(this.featuresFN);
        for (IObject feature : featuresFromJson) {
            String name = (String) feature.getValue(this.nameFN);
            features.put(name, new Feature(
                            (String) feature.getValue(this.nameFN),
                            (String) feature.getValue(this.groupFN),
                            (String) feature.getValue(this.versionFN),
                            new Path(jsonFile.getParent())
                    )
            );
        }

        return features;
    }
}
