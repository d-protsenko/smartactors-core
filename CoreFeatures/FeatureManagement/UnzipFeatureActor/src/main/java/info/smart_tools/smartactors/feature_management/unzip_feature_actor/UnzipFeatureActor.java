package info.smart_tools.smartactors.feature_management.unzip_feature_actor;

import info.smart_tools.smartactors.base.interfaces.iaction.IBiFunction;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private final IFieldName featureNameFN;

    private final static String CONFIG_FILE_NAME = "config.json";
    private final static String EXTENSION_SEPARATOR = ".";
    private final static String IOBJECT_FACTORY_STRATEGY_NAME = "info.smart_tools.smartactors.iobject.iobject.IObject";
    private final static String FIELD_NAME_FACTORY_STARTEGY_NAME =
            "info.smart_tools.smartactors.iobject.ifield_name.IFieldName";
    private final static String ARCHIVE_POSTFIX = "archive";
    private final static String GROUP_AND_NAME_DELIMITER = ":";
    private final static String END_OF_INPUT_DELIMITER = "\\Z";
    private final static String NAME_OF_CHECK_FILE = ".checkfile";

    private final Map<String, IBiFunction<File, IFeature, File>> unzipFunctions;

    /**
     * Default constructor
     * @throws ResolutionException if any errors occurred on resolution of IOC dependencies
     */
    public UnzipFeatureActor()
            throws ResolutionException {
        this.dependenciesFieldName = IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "afterFeatures");
        this.featureNameFN =         IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "featureName");
        this.featureVersionFN =      IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "featureVersion");

        //TODO: need refactoring. This actions would be took out to the plugin.
        this.unzipFunctions = new HashMap<String, IBiFunction<File, IFeature, File>>(){{
            put("zip", (file, feature) -> {
                try {
                    return unzip0(file, feature);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            put("jar", (file, feature) -> {
                try {
                    return copyJar(file, feature);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }};
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
            if (null == feature.getDependencies()) {
                System.out.println("[INFO] Start unzipping/copying feature - '" + feature.getName() + "'.");
                File f = new File(feature.getLocation().toString());
                IBiFunction<File, IFeature, File> function = this.unzipFunctions.get(getExtension(f));
                if (null != function) {
                    File configFile = function.execute(f, feature);
                    updateFeature(configFile, feature);
                    System.out.println("[OK] -------------- Feature '" + feature.getName() + "' has been unzipped/copied successful.");
                }
            }
        } catch (Throwable e) {
            feature.setFailed(true);
            System.out.println("[FAILED] ---------- Feature '" + feature.getName() + "' unzipping/copying has been aborted with exception:");
            System.out.println(e);
        }
    }

    private File unzip0(final File f, final IFeature feature)
            throws Exception {
        String destination = f.getPath();
        if (f.getPath().lastIndexOf(File.separator) >= 0) {
            destination = f.getPath().substring(0, f.getPath().lastIndexOf(File.separator));
        }
        try {
            if (checkFeatureWasUpdated(f, feature)) {
                ZipFile zipFile = new ZipFile(f);
                zipFile.extractAll(destination);
                createCheckFile(
                        getFeatureDirectory(f).toFile()
                );
            }

            return Paths.get(this.getFeatureDirectory(f).toString(), CONFIG_FILE_NAME).toFile();
        } catch (ZipException e) {
            throw new Exception("Unsupported feature format: broken archive.", e);
        } catch (NoSuchElementException e) {
            throw new Exception("Unsupported feature format: config.json not found or it's broken.", e);
        }
    }

    private File copyJar(final File f, final IFeature feature)
            throws Exception {
        try {
            java.nio.file.Path dir = Files.createDirectories(
                    getFeatureDirectory(f)
            );
            if (checkFeatureWasUpdated(f, feature)) {
                Files.copy(
                        Paths.get(f.getPath()),
                        Paths.get(dir.toString(), f.getName()),
                        StandardCopyOption.REPLACE_EXISTING
                );
                ZipFile zipFile = new ZipFile(f);
                List<FileHeader> headers = zipFile.getFileHeaders();
                FileHeader configFileHeader = headers
                        .stream()
                        .filter(fh -> fh.getFileName().endsWith(CONFIG_FILE_NAME))
                        .findFirst()
                        .get();
                if (null != configFileHeader) {
                    zipFile.extractFile(configFileHeader, dir.toString());
                }
                createCheckFile(dir.toFile());
            }

            return new File(Paths.get(dir.toString(), CONFIG_FILE_NAME).toString());
        } catch (ZipException e) {
            throw new Exception("Unsupported feature format: broken archive.", e);
        } catch (NoSuchElementException e) {
            throw new Exception("Unsupported feature format: config.json not found or it's broken.", e);
        }
    }

    private boolean checkFeatureWasUpdated(final File zippedFeature, final IFeature feature) {
        try {
            File destinationDir = this.getFeatureDirectory(zippedFeature).toFile();
            if (destinationDir.exists() && destinationDir.isDirectory()) {
                File checkFile = Paths.get(destinationDir.getPath(), NAME_OF_CHECK_FILE).toFile();
                if (checkFile.exists() && zippedFeature.lastModified() < checkFile.lastModified()) {
                    System.out.println(
                            "[OK] -------------- Unzipping/copying of the feature '" +
                            feature.getName() +
                            "' was skipped because the directory contains an actual state of this feature."
                    );
                    return false;
                }
            }
        } catch (Exception e) {
            return true;
        }

        return true;
    }

    private java.nio.file.Path getFeatureDirectory(final File f) {
        String location = f.getPath();
        if (f.getPath().lastIndexOf(File.separator) >= 0) {
            location = f.getPath().substring(0, f.getPath().lastIndexOf(File.separator));
        }
        String featureName = f.getName().substring(0, f.getName().lastIndexOf(EXTENSION_SEPARATOR));
        if (featureName.endsWith("-" + ARCHIVE_POSTFIX)) {
            featureName = featureName.substring(0, featureName.length() - 8);
        }
        return Paths.get(
                location, featureName
        );
    }

    private void updateFeature(final File f, final IFeature feature)
            throws Exception {
        String content = new Scanner(f).useDelimiter(END_OF_INPUT_DELIMITER).next();
        IObject config = IOC.resolve(Keys.getOrAdd(IOBJECT_FACTORY_STRATEGY_NAME), content);
        List<String> dependencies = (List<String>) config.getValue(this.dependenciesFieldName);
        String featureNameWithGroup = (String) config.getValue(this.featureNameFN);
        String featureVersion = (String) config.getValue(this.featureVersionFN);
        String[] groupAndName = featureNameWithGroup.split(GROUP_AND_NAME_DELIMITER);
        if (groupAndName.length < 2) {
            throw new Exception("Unsupported attribute 'featureName' in the config.json. Should follows the pattern [groupId:featureName].");
        }
        String name = groupAndName[1];
        String groupId = groupAndName[0];
        Set<String> dependenciesSet = new HashSet<>(dependencies);
        feature.setDependencies(dependenciesSet);
        feature.setLocation(new Path(f.getParent()));
        feature.setName(name);
        feature.setGroupId(groupId);
        feature.setVersion(featureVersion);
    }

    private String getExtension(final File f) {
        return f.getName().substring(f.getName().lastIndexOf(EXTENSION_SEPARATOR) + 1);
    }

    private void createCheckFile(final File directory)
            throws Exception {
        File f = Paths.get(directory.getPath(), NAME_OF_CHECK_FILE).toFile();
        if (f.exists()) {
            f.setLastModified(System.currentTimeMillis());
        } else {
            Files.createFile(f.toPath());
        }
    }

}
