package info.smart_tools.smartactors.feature_management.feature_creator_actor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
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

    /**
     * Default constructor
     * @throws ResolutionException if any errors occurred on IOC resolution
     */
    public FeaturesCreatorActor()
            throws ResolutionException {
        this.featuresFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "features");
        this.repositoriesFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "repositories");

        this.nameFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "name");
        this.groupFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "group");
        this.versionFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "version");
        this.featureLocationFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "featureLocation");
    }

    /**
     * Gathers feature descriptions and puts them to the message
     * @param wrapper the wrapped message for getting needed data and storing result
     * @throws FeatureCreationException if any errors occurred on gathering feature description and on message creation
     */
    public void createMessageByFile(final CreateMessageWrapper wrapper)
            throws FeatureCreationException {
        try {
            File file = new File(wrapper.getFileName());
            if (file.exists() && !file.isDirectory()) {
                if (file.getName().endsWith(".zip")) {
                    wrapper.setJsonFeaturesDescription(createJsonFeatureDescriptionByZip(file, wrapper.getObservedDirectory()));
                    wrapper.setJsonRepositoriesDescription(new ArrayList<IObject>());
                }
                if (file.getName().endsWith(".json")) {
                    wrapper.setJsonFeaturesDescription(getDescription(file, this.featuresFN));
                    wrapper.setJsonRepositoriesDescription(getDescription(file, this.repositoriesFN));
                }
            }
        } catch (ReadValueException | ChangeValueException e) {
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
            List<IObject> repositoryStorage = IOC.resolve(Keys.getOrAdd("feature-repositories"));

            if (null != repositories) {
                for (IObject repository : repositories) {
                    repositoryStorage.add(repository);
                }
            }
            List<IObject> featuresFromJson = wrapper.getFeaturesDescription();
            if (null != featuresFromJson) {
                for (IObject feature : featuresFromJson) {
                    String name = (String) feature.getValue(this.nameFN);
                    if (null != feature.getValue(this.groupFN)) {
                        features.put(name, new Feature(
                                        name,
                                        (String) feature.getValue(this.groupFN),
                                        (String) feature.getValue(this.versionFN),
                                        (IPath) feature.getValue(this.featureLocationFN)
                                )
                        );
                    } else {
                        features.put(name, new Feature(
                                        name,
                                        null,
                                        (IPath) feature.getValue(this.featureLocationFN)
                                )
                        );
                    }
                }
            }

            wrapper.setFeatures(features.values());
        } catch (ResolutionException | ChangeValueException | ReadValueException | InvalidArgumentException e) {
            throw new FeatureCreationException(e);
        }
    }

    private List<IObject> createJsonFeatureDescriptionByZip(final File file, final String observedDirectory)
            throws FeatureCreationException {
        try {
            String name = file.getName().split("-\\d\\.\\d\\.\\d-")[0];
            List<IObject> featuresDescription = new ArrayList<>();
            IObject featureDescription = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
            featureDescription.setValue(this.nameFN, name);
            featureDescription.setValue(this.groupFN, null);
            featureDescription.setValue(this.versionFN, null);
            featureDescription.setValue(this.featureLocationFN, new Path(observedDirectory + File.separator + file.getPath()));
            featuresDescription.add(featureDescription);

            return featuresDescription;
        } catch (ChangeValueException | InvalidArgumentException | ResolutionException e) {
            throw new FeatureCreationException(e);
        }
    }

    private List<IObject> getDescription(final File file, final IFieldName fieldName)
            throws FeatureCreationException {
        try {
            IObject jsonConfig = IOC.resolve(
                    Keys.getOrAdd(IObject.class.getCanonicalName()), new Scanner(file).useDelimiter("\\Z").next()
            );
            return (List<IObject>) jsonConfig.getValue(fieldName);
        } catch (ResolutionException | IOException | ReadValueException | InvalidArgumentException e) {
            throw new FeatureCreationException(e);
        }
    }
}
