package info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager;

import com.jcabi.aether.Aether;
import info.smart_tools.smartactors.base.path.Path;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature.IFeatureState;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.exception.FeatureManagementException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by sevenbits on 11/15/16.
 */
public class DownloadFeatureTask implements ITask {

    private IFeatureManager featureManager;
    private IFeature feature;

    public DownloadFeatureTask(final IFeatureManager manager, final IFeature feature)
            throws ResolutionException {

        this.featureManager = manager;
        this.feature = feature;
    }

    public void execute()
            throws TaskExecutionException {
        try {
            if (null == this.feature.getDependencies() && null != this.feature.getGroupId() && null != this.feature.getVersion()) {
                System.out.println("[INFO] Start downloading feature - '" + feature.getName() + "'.");
                download();
                System.out.println("[OK] -------------- Feature '" + this.feature.getName() + "' has been downloaded successful.");
            }
            ((IFeatureState) this.feature.getStatus()).setLastSuccess(true);
        } catch (Throwable e) {
            ((IFeatureState) this.feature.getStatus()).setLastSuccess(false);
            System.out.println("[FAILED] ---------- Feature '" + this.feature.getName() + "' downloading has been aborted with exception:");
            System.out.println(e);
        }
        try {
            this.featureManager.onCompleteFeatureOperation(this.feature);
        } catch (FeatureManagementException e) {
            System.out.println(e);
        }
    }

    private void download()
            throws Exception {
        try {
            File local = new File("downloads/");
            Collection<RemoteRepository> repositories = new ArrayList<>();


            Collection<RemoteRepository> remotes = FeatureManagerGlobal.getRepositories().stream().map(
                    a-> new RemoteRepository(a.getRepositoryId(), a.getRepositoryType(), a.getRepositoryUrl())
            ).collect(Collectors.toList());

            List<Artifact> artifacts = new Aether(remotes, local).resolve(
                    new DefaultArtifact(
                            this.feature.getGroupId(),
                            this.feature.getName(),
                            "",
                            "zip",
                            this.feature.getVersion()
                    ),
                    "runtime"
            );
            File artifact = artifacts.get(0).getFile();
            String fileName = artifact.getName();
            File location = new File (this.feature.getFeatureLocation() + File.separator + fileName);
            Files.copy(artifact.toPath(), location.toPath());
            this.feature.setFeatureLocation(new Path(location));
        } catch (Throwable e) {

        }
    }
}
