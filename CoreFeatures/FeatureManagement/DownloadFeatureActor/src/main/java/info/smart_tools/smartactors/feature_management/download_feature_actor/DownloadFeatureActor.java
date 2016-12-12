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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by sevenbits on 12/6/16.
 */
public class DownloadFeatureActor {

    private final IFieldName repositoryIdFN;
    private final IFieldName repositoryTypeFN;
    private final IFieldName repositoryUrlFN;

    public DownloadFeatureActor()
            throws ResolutionException {
        this.repositoryIdFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "repositoryId");
        this.repositoryTypeFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "type");
        this.repositoryUrlFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "url");
    }

    public void download(final DownloadFeatureWrapper wrapper)
            throws DownloadFeatureException {
        IFeature feature;
        try {
            feature = wrapper.getFeature();
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

    private void download0(IFeature feature) {
        try {
            File local = new File("downloads/");
            //Collection<RemoteRepository> repositories = new ArrayList<>();

            List<IObject> repositories = IOC.resolve(Keys.getOrAdd("feature-repositories"));

            Collection<RemoteRepository> remotes = repositories.stream().map(
                    a-> {
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
                            "zip",
                            feature.getVersion()
                    ),
                    "runtime"
            );
            File artifact = artifacts.get(0).getFile();
            String fileName = artifact.getName();
            File location = new File (feature.getFeatureLocation() + File.separator + fileName);
            Files.copy(artifact.toPath(), location.toPath());
            feature.setFeatureLocation(new Path(location));
        } catch (Throwable e) {

        }
    }
}
