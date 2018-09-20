package info.smart_tools.smartactors.feature_management.feature_creator_actor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IBiAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.interfaces.ipath.IPath;
import info.smart_tools.smartactors.base.path.Path;
import info.smart_tools.smartactors.feature_management.feature.Feature;
import info.smart_tools.smartactors.feature_management.feature_creator_actor.exception.FeatureCreationException;
import info.smart_tools.smartactors.feature_management.feature_creator_actor.wrapper.CreateFeaturesWrapper;
import info.smart_tools.smartactors.feature_management.feature_creator_actor.wrapper.CreateMessageWrapper;
import info.smart_tools.smartactors.feature_management.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Actor that creates instance of {@link IFeature}
 * by given feature location and by given other feature description.
 */
public class FeaturesCreatorActor {

    private final IFieldName nameFN;
    private final IFieldName groupFN;
    private final IFieldName versionFN;
    private final IFieldName featureLocationFN;

    private final IFieldName featuresFN;
    private final IFieldName repositoriesFN;
    private final IFieldName packageTypeFN;

    private final static String END_OF_INPUT_DELIMITER = "\\Z";
    private final static String EXTENSION_SEPARATOR = ".";
    private final static String IOBJECT_FACTORY_STRATEGY_NAME = "info.smart_tools.smartactors.iobject.iobject.IObject";
    private final static String FIELD_NAME_FACTORY_STARTEGY_NAME =
            "info.smart_tools.smartactors.iobject.ifield_name.IFieldName";
    private final static String IOC_FEATURE_REPOSITORY_STORAGE_NAME = "feature-repositories";

    //TODO: this parameters would be took out into the config.json as actor arguments
    private final static String FEATURE_VERSION_PATTERN = "-\\d+\\.\\d+\\.\\d+";

    private final Map<String, IBiAction<File, CreateMessageWrapper>> creationFunctions;

    /**
     * Default constructor
     * @throws ResolutionException if any errors occurred on IOC resolution
     */
    public FeaturesCreatorActor()
            throws ResolutionException {
        this.featuresFN =        IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "features");
        this.repositoriesFN =    IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "repositories");
        this.nameFN =            IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "name");
        this.groupFN =           IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "group");
        this.versionFN =         IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "version");
        this.featureLocationFN = IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "featureLocation");
        this.packageTypeFN =     IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "packageType");

        //TODO: need refactoring. This actions would be took out to the plugin.
        this.creationFunctions = new HashMap<String, IBiAction<File, CreateMessageWrapper>>(){{
            put("zip", (f, w) -> {
                try {
                    w.setJsonFeaturesDescription(createJsonFeatureDescriptionByZip(f));
                    w.setJsonRepositoriesDescription(new ArrayList<>());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            put("jar", (f, w) -> {
                try {
                    w.setJsonFeaturesDescription(createJsonFeatureDescriptionByZip(f));
                    w.setJsonRepositoriesDescription(new ArrayList<>());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            put("json",  (f, w) -> {
                try {
                    w.setJsonFeaturesDescription(getProperty(f, featuresFN));
                    w.setJsonRepositoriesDescription(getProperty(f, repositoriesFN));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }};
    }

    /**
     * Gathers feature descriptions and puts them to the message
     * @param wrapper the wrapped message for getting needed data and storing result
     * @throws FeatureCreationException if any errors occurred on gathering feature description and on message creation
     */
    public void createMessageByFile(final CreateMessageWrapper wrapper)
            throws FeatureCreationException {
        try {
            File file = Paths.get(wrapper.getObservedDirectory(), wrapper.getFileName()).toFile();
            if (file.exists() && !file.isDirectory()) {

                IBiAction<File, CreateMessageWrapper> action = this.creationFunctions.get(
                        getExtension(file)
                );
                if (null != action) {
                    action.execute(file, wrapper);
                }
            }
        } catch (ReadValueException | InvalidArgumentException | ActionExecuteException e) {
            throw new FeatureCreationException("Could not create features by given file.", e);
        }
    }

    /**
     * Creates instance of {@link IFeature} by feature descriptions and puts them to the message
     * @param wrapper the wrapped message for getting needed data and storing result
     * @throws FeatureCreationException if any errors occurred on feature creation
     */
    public void createFeaturesByMessage(final CreateFeaturesWrapper wrapper)
            throws FeatureCreationException {
        try {
            Map<String, IFeature> features = new HashMap<>();
            List<IObject> repositories = wrapper.getRepositoriesDescription();
            List<IObject> repositoryStorage = IOC.resolve(Keys.getOrAdd(IOC_FEATURE_REPOSITORY_STORAGE_NAME));

            if (null != repositories) {
                repositoryStorage.addAll(repositories);
            }
            List<IObject> featuresFromJson = wrapper.getFeaturesDescription();
            if (null != featuresFromJson) {
                for (IObject feature : featuresFromJson) {
                    String name = (String) feature.getValue(this.nameFN);
                    String packageType = (String) feature.getValue(this.packageTypeFN);
                    features.put(name, new Feature(
                            (String) feature.getValue(this.groupFN),
                            (String) feature.getValue(this.nameFN),
                            (String) feature.getValue(this.versionFN),
                            null,
                            new Path(wrapper.getFeatureDirectory()),
                            packageType
                    ));
                }
            }

            wrapper.setFeatures(features.values());
        } catch (ResolutionException | ChangeValueException | ReadValueException | InvalidArgumentException e) {
            throw new FeatureCreationException(e);
        }
    }

    private List<IObject> createJsonFeatureDescriptionByZip(final File file)
            throws FeatureCreationException {
        try {
            String packageType = getExtension(file);
            String name = file.getName().split(FEATURE_VERSION_PATTERN)[0];
            List<IObject> featuresDescription = new ArrayList<>();
            IObject featureDescription = IOC.resolve(Keys.getOrAdd(IOBJECT_FACTORY_STRATEGY_NAME));
            featureDescription.setValue(this.nameFN, name);
            featureDescription.setValue(this.groupFN, null);
            featureDescription.setValue(this.versionFN, null);
            featureDescription.setValue(this.featureLocationFN, new Path(file.getPath()));
            featureDescription.setValue(this.packageTypeFN, packageType);
            featuresDescription.add(featureDescription);

            return featuresDescription;
        } catch (ChangeValueException | InvalidArgumentException | ResolutionException e) {
            throw new FeatureCreationException(e);
        }
    }

    private List<IObject> getProperty(final File file, final IFieldName fieldName)
            throws FeatureCreationException {
        try {
            IObject jsonConfig = IOC.resolve(
                    Keys.getOrAdd(IOBJECT_FACTORY_STRATEGY_NAME),
                    new Scanner(file).useDelimiter(END_OF_INPUT_DELIMITER).next()
            );
            return (List<IObject>) jsonConfig.getValue(fieldName);
        } catch (ResolutionException | IOException | ReadValueException | InvalidArgumentException e) {
            throw new FeatureCreationException(e);
        }
    }

    private String getExtension(final File f) {
        return f.getName().substring(f.getName().lastIndexOf(EXTENSION_SEPARATOR) + 1);
    }
}
