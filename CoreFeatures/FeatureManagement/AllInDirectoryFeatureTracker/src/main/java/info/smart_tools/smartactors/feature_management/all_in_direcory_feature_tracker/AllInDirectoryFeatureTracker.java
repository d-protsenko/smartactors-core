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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Actor that scans specific directory,
 * finds features descriptions (in zip archives or in specific json files),
 * creates feature and puts them to the message
 */
public class AllInDirectoryFeatureTracker {

    private final IFieldName featureNameFN;
    private final IFieldName featureVersionFN;
    private final IFieldName afterFeaturesFN;
    private final IFieldName repositoriesFN;
    private final IFieldName featuresFN;
    private final IFieldName nameFN;
    private final IFieldName groupFN;
    private final IFieldName versionFN;
    private final IFieldName packageTypeFN;

    private final static String END_OF_INPUT_DELIMITER = "\\Z";
    private final static String CONFIG_FILE_NAME = "config.json";
    private final static String EXTENSION_SEPARATOR = ".";
    private final static String IOBJECT_FACTORY_STRATEGY_NAME = "info.smart_tools.smartactors.iobject.iobject.IObject";
    private final static String FIELD_NAME_FACTORY_STARTEGY_NAME =
            "info.smart_tools.smartactors.iobject.ifield_name.IFieldName";
    private final static String IOC_FEATURE_REPOSITORY_STORAGE_NAME = "feature-repositories";

    //TODO: this parameters would be took out into the config.json as actor arguments
    private final static String FEATURE_LIST_FILE_NAME = "features.json";
    private final static String DEF_PACKAGE_TYPE = "jar";
    private final static String FEATURE_VERSION_PATTERN = "-\\d+\\.\\d+\\.\\d+";
    private final static List<String> FILE_TYPE_LIST = Arrays.asList("zip", "jar");
    private final static String FEATURE_NAME_DELIMITER = ":";

    /**
     * Default constructor
     *
     * @throws ResolutionException if any errors occurred on IOC resolution
     */
    public AllInDirectoryFeatureTracker()
            throws ResolutionException {
        this.featureNameFN =    IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "featureName");
        this.featureVersionFN = IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "featureVersion");
        this.afterFeaturesFN =  IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "afterFeatures");
        this.repositoriesFN =   IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "repositories");
        this.featuresFN =       IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "features");
        this.nameFN =           IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "name");
        this.groupFN =          IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "group");
        this.versionFN =        IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "version");
        this.packageTypeFN =    IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "packageType");
    }

    /**
     * Scans specific directory for features descriptions, creates features and puts them to the message for post processing
     *
     * @param wrapper the wrapped message for getting needed data and storing result
     * @throws FeatureTrackerException if any errors occurred on feature creation
     */
    public void addFeatures(final FeatureTrackerWrapper wrapper)
            throws FeatureTrackerException {
        try {
            String path = wrapper.getPath();

            // get feature list from json file
            File downloadList = Paths.get(path, FEATURE_LIST_FILE_NAME).toFile();
            Map<String, IFeature> features = new HashMap<>();
            features.putAll(this.parseFeatureList(downloadList));

            // get feature list from existing zip and jar files by the given path
            File[] fZipped = new File(path).listFiles(
                    (item, name) -> FILE_TYPE_LIST.contains(this.getExtension(new File(name)))
            );
            if (null != fZipped) {
                Arrays.stream(fZipped).map(m -> {
                            try {
                                return createZippedFeature(m);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                ).forEach(i -> features.put(i.getName(), i));
            }

            wrapper.setFeatures(features.values());
        } catch (Throwable e) {
            throw new FeatureTrackerException(e);
        }
    }

    private IFeature createFeature(final File f)
            throws Exception {
        File jsonFile = Paths.get(f.getPath(), CONFIG_FILE_NAME).toFile();
        if (!jsonFile.exists()) {
            throw new Exception("File config.json not found in the folder :" + f.getPath());
        }

        IObject jsonConfig = IOC.resolve(
                Keys.getOrAdd(IOBJECT_FACTORY_STRATEGY_NAME),
                new Scanner(jsonFile).useDelimiter(END_OF_INPUT_DELIMITER).next()
        );

        String featureName = (String) jsonConfig.getValue(this.featureNameFN);
        String[] featureNames = featureName.split(FEATURE_NAME_DELIMITER);
        if (featureNames.length < 2) {
            throw new Exception("Wrong feature name format '"+featureName+"' in "+jsonFile.getPath()+" .");
        }
        String groupId = featureNames[0];
        String name = featureNames[1];
        String version = featureNames.length > 2 ? featureNames[2] : null;

        Set<String> dependencies = new HashSet<String>((List) jsonConfig.getValue(this.afterFeaturesFN));

        return new Feature(groupId, name, version, dependencies, new Path(f.getPath()), null);
    }

    private IFeature createZippedFeature(final File f)
            throws Exception {

        String name = f.getName().split(FEATURE_VERSION_PATTERN)[0];
        Pattern pattern = Pattern.compile(FEATURE_VERSION_PATTERN);
        Matcher matcher = pattern.matcher(f.getName());
        matcher.find();
        String version = matcher.group().substring(1);
        return new Feature(
                null,
                name,
                version,
                null,
                new Path(f.getPath()),
                this.getExtension(f)
        );
    }

    private Map<String, IFeature> parseFeatureList(final File jsonFile)
            throws Exception {
        Map<String, IFeature> features = new HashMap<>();
        if (!jsonFile.exists()) {
            return features;
        }

        IObject jsonConfig = IOC.resolve(
                Keys.getOrAdd(IOBJECT_FACTORY_STRATEGY_NAME),
                new Scanner(jsonFile).useDelimiter(END_OF_INPUT_DELIMITER).next()
        );

        List<IObject> repositories = (List<IObject>) jsonConfig.getValue(this.repositoriesFN);
        List<IObject> repositoryStorage = IOC.resolve(Keys.getOrAdd(IOC_FEATURE_REPOSITORY_STORAGE_NAME));

        repositoryStorage.addAll(repositories);
        List<IObject> featuresFromJson = (List<IObject>) jsonConfig.getValue(this.featuresFN);
        for (IObject feature : featuresFromJson) {
            String name = (String) feature.getValue(this.nameFN);
            String packageType = (String) feature.getValue(this.packageTypeFN);
            features.put(name, new Feature(
                            (String) feature.getValue(this.groupFN),
                            (String) feature.getValue(this.nameFN),
                            (String) feature.getValue(this.versionFN),
                            null,
                            new Path(jsonFile.getParent()),
                            null != packageType ? packageType : DEF_PACKAGE_TYPE
                    )
            );
        }

        return features;
    }

    private String getExtension(final File f) {
        return f.getName().substring(f.getName().lastIndexOf(EXTENSION_SEPARATOR) + 1);
    }
}
