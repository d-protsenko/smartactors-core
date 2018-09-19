package info.smart_tools.smartactors.feature_management.download_feature_actor;

import com.jcabi.aether.Aether;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.path.Path;
import info.smart_tools.smartactors.feature_management.download_feature_actor.exception.DownloadFeatureException;
import info.smart_tools.smartactors.feature_management.download_feature_actor.wrapper.DownloadFeatureWrapper;
import info.smart_tools.smartactors.feature_management.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Actor that downloads artifact from remote repository.
 */
public class DownloadFeatureActor {

    private final IFieldName repositoryIdFN;
    private final IFieldName repositoryTypeFN;
    private final IFieldName repositoryUrlFN;

    private final static String DOWNLOAD_DIRECTORY = "downloads";
    private final static String IOC_FEATURE_REPOSITORY_STORAGE_NAME = "feature-repositories";
    private final static String MAVEN_ARTIFACT_SCOPE = "runtime";
    private final static String ARCHIVE_POSTFIX = "archive";

    //TODO: this parameters would be took out into the config.json as actor arguments
    private static final String DEF_PACKAGE_TYPE = "jar";
    private final static String FIELD_NAME_FACTORY_STARTEGY_NAME =
            "info.smart_tools.smartactors.iobject.ifield_name.IFieldName";
    private final static List<String> FILE_TYPE_LIST = Arrays.asList("zip", "jar");

    /**
     * Default constructor
     * @throws ResolutionException if any errors occurred on IOC resolution
     */
    public DownloadFeatureActor()
            throws ResolutionException {
        this.repositoryIdFN = IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "repositoryId");
        this.repositoryTypeFN = IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "type");
        this.repositoryUrlFN = IOC.resolve(Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "url");
    }

    /**
     * Download feature (artifact) from remote repository
     * @param wrapper the wrapped message for getting needed data and storing result
     * @throws DownloadFeatureException if any errors occurred on feature downloading
     */
    public void download(final DownloadFeatureWrapper wrapper)
            throws DownloadFeatureException {
        IFeature feature;
        try {
            feature = wrapper.getFeature();
            //TODO: need refactoring
            for(String type : FILE_TYPE_LIST) {
                if (
                        Paths.get(
                                feature.getLocation().getPath(),
                                feature.getName() + "-" + feature.getVersion() + "." + type
                        ).toFile().exists()
                ) {
                   return;
                }
                if (
                        Paths.get(
                                feature.getLocation().getPath(),
                                feature.getName() + "-" + feature.getVersion() + "-" + ARCHIVE_POSTFIX + "." + type
                        ).toFile().exists()
                        ) {
                    return;
                }
            }
        } catch (ReadValueException e) {
            throw new DownloadFeatureException("Feature should not be null.");
        }
        try {
            if (null == feature.getDependencies() && null != feature.getGroupId() && null != feature.getVersion()) {
                System.out.println("[INFO] Start downloading feature - '" + feature.getName() + "'.");
                download0(feature);
                System.out.println("[OK] -------------- Feature '" + feature.getName() + "' has been downloaded successful.");
            }
        } catch (Throwable e) {
            feature.setFailed(true);
            System.out.println("[FAILED] ---------- Feature '" + feature.getName() + "' downloading has been aborted with exception:");
            System.out.println(e);
        }
    }

    private void download0(final IFeature feature)
            throws Exception {
        try {
            File local = new File(DOWNLOAD_DIRECTORY);
            List<IObject> repositories = IOC.resolve(Keys.getOrAdd(IOC_FEATURE_REPOSITORY_STORAGE_NAME));
            Collection<RemoteRepository> remotes = repositories.stream().map(
                    a -> {
                        try {
                            return new RemoteRepository(
                                    (String) a.getValue(this.repositoryIdFN),
                                    (String) a.getValue(this.repositoryTypeFN),
                                    (String) a.getValue(this.repositoryUrlFN));
                        } catch (ReadValueException | InvalidArgumentException e) {
                            throw new RuntimeException(e);
                        }
                    }
            ).collect(Collectors.toList());

            List<Artifact> artifacts = new Aether(remotes, local).resolve(
                    new DefaultArtifact(
                            feature.getGroupId(),
                            feature.getName(),
                            "",
                            feature.getPackageType() != null ? feature.getPackageType() : DEF_PACKAGE_TYPE,
                            feature.getVersion()
                    ),
                    MAVEN_ARTIFACT_SCOPE
            );
            File artifact = artifacts.get(0).getFile();
            String fileName = artifact.getName();
            File location = Paths.get(feature.getLocation().getPath(), fileName).toFile();
            Files.copy(artifact.toPath(), location.toPath());
            feature.setLocation(new Path(location));
        } catch (Throwable e) {
            throw new Exception(e);
        }
    }
}
