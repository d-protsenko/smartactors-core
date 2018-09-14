package info.smart_tools.smartactors.feature_management.unzip_feature_actor;

import info.smart_tools.smartactors.base.path.Path;
import info.smart_tools.smartactors.class_management.class_loader_management.VersionManager;
import info.smart_tools.smartactors.feature_management.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.feature_management.unzip_feature_actor.exception.UnzipFeatureException;
import info.smart_tools.smartactors.feature_management.unzip_feature_actor.wrapper.UnzipFeatureWrapper;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

/**
 * Actor that unpack feature
 */
public class UnzipFeatureActor {

    private final IFieldName featureNameFN;
    private final IFieldName featureVersionFN;
    private final IFieldName dependenciesFieldName;

    /**
     * Default constructor
     * @throws ResolutionException if any errors occurred on resolution of IOC dependencies
     */
    public UnzipFeatureActor()
            throws ResolutionException {
        this.featureNameFN = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "featureName");
        this.featureVersionFN = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "featureVersion");
        this.dependenciesFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "afterFeatures");
    }

    /**
     * Unpacks feature
     * @param wrapper the wrapped message for getting needed data and storing result
     * @throws UnzipFeatureException if any errors occurred on unpacking feature
     */
    public void unzip(final UnzipFeatureWrapper wrapper)
            throws UnzipFeatureException {
        IFeature feature;
        try {
            feature = wrapper.getFeature();
        } catch (ReadValueException e) {
            throw new UnzipFeatureException("Feature should not be null.");
        }
        try {
            if (null == feature.getDependencies() && feature.getLocation().toString().endsWith(".zip")) {
                System.out.println("[INFO] Start unzipping feature - '" + feature.getName() + "'.");
                File f = new File(feature.getLocation().toString());
                File configFile = unzip0(f);
                updateFeature(configFile, feature);
                System.out.println("[OK] -------------- Feature '" + feature.getName() + "' has been unzipped successful.");
            }
        } catch (Throwable e) {
            feature.setFailed(true);
            System.out.println("[FAILED] ---------- Feature '" + feature.getName() + "' unzipping has been aborted with exception:");
            System.out.println(e);
        }
    }

    private File unzip0(final File f)
            throws Exception {
        String destination = f.getPath();
        if (f.getPath().lastIndexOf('/') >= 0) {
            destination = f.getPath().substring(0, f.getPath().lastIndexOf('/'));
        }
        try {
            ZipFile zipFile = new ZipFile(f);
            zipFile.extractAll(destination);
            List<FileHeader> headers = zipFile.getFileHeaders();
            FileHeader configFileHeader = headers.stream().filter(fh -> fh.getFileName().endsWith("config.json")).findFirst().get();

            return new File(destination + File.separator + configFileHeader.getFileName());
        } catch (ZipException e) {
            throw new Exception("Unsupported feature format: broken archive.", e);
        } catch (NoSuchElementException e) {
            throw new Exception("Unsupported feature format: config.json not found or it's broken.", e);
        }
    }

    private void updateFeature(final File f, final IFeature feature)
            throws Exception {
        String content = new Scanner(f).useDelimiter("\\Z").next();
        IObject config = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"), content);
        String featureName = (String) config.getValue(this.featureNameFN);
        feature.setName(featureName);
        String featureVersion = (String) config.getValue(this.featureVersionFN);
        feature.setVersion(featureVersion);
        String featureID = VersionManager.addItem(feature.getName(), feature.getVersion());
        feature.setID(featureID);
        List<String> dependencies = (List<String>) config.getValue(this.dependenciesFieldName);
        Set<String> dependenciesSet = new HashSet<>(dependencies);
        feature.setDependencies(dependenciesSet);
        feature.setLocation(new Path(f.getParent()));
    }
}
