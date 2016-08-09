package info.smart_tools.smartactors.core.dependency_resolving_feature_manager;

import info.smart_tools.smartactors.core.ifeature_manager.IFeature;
import info.smart_tools.smartactors.core.ifeature_manager.IFeatureManager;
import info.smart_tools.smartactors.core.ifeature_manager.exception.FeatureManagementException;
import info.smart_tools.smartactors.core.ifilesystem_tracker.IFilesystemTracker;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ipath.IPath;
import info.smart_tools.smartactors.core.path.Path;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.transport.wagon.WagonTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of feature manager that automatically resolves dependencies of required items (using Aether library).
 */
public class DependencyResolvingFeatureManager implements IFeatureManager {
    private String localRepoPath;
    private Map<String, String> remotes;
    private RepositorySystem repositorySystem;
    private RepositorySystemSession session;
    private final Object lock;
    private List<RemoteRepository> remoteRepositories;

    /**
     * The constructor.
     *
     * @param localRepoPath    path to local repository
     * @param remotes          URL's of remote repositories (map from repository name to URL)
     * @throws InvalidArgumentException if {@code localRepoPath} is {@code null}
     * @throws InvalidArgumentException if {@code remotes} is {@code null}
     */
    public DependencyResolvingFeatureManager(final String localRepoPath, final Map<String, String> remotes)
            throws InvalidArgumentException {
        if (null == localRepoPath) {
            throw new InvalidArgumentException("Local repository path should not be null.");
        }

        if (null == remotes) {
            throw new InvalidArgumentException("List of remote repositories URLs should not be null.");
        }

        this.localRepoPath = localRepoPath;
        this.remotes = remotes;
        this.lock = new Object();

        setupRemotes();
        newRepositorySystem();
        newSession();
    }

    @Override
    public IFeature newFeature(final String name, final IFilesystemTracker filesystemTracker) throws FeatureManagementException {
        try {
            return new Feature(this, name);
        } catch (InvalidArgumentException e) {
            throw new FeatureManagementException("Cold not create a feature.", e);
        }
    }

    private void newRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();

        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        locator.addService(TransporterFactory.class, WagonTransporterFactory.class);

        repositorySystem = locator.getService(RepositorySystem.class);
    }

    private void newSession() {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepo = new LocalRepository(localRepoPath);
        session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(session, localRepo));

        this.session = session;
    }

    private void setupRemotes() {
        final List<RemoteRepository> remoteRepositories = new ArrayList<>(remotes.size());

        for (Map.Entry<String, String> remote : remotes.entrySet()) {
            remoteRepositories.add(new RemoteRepository.Builder(remote.getKey(), "default", remote.getValue()).build());
        }

        this.remoteRepositories = remoteRepositories;
    }

    /**
     * Resolve artifacts and their dependencies by given identifiers.
     *
     * @param artifactIds    list of artifact identifiers in format "group:id:version"
     * @return list of paths to resolved artifacts and their dependencies
     * @throws FeatureManagementException if any error occurs
     */
    List<IPath> resolveArtifacts(final List<String> artifactIds) throws FeatureManagementException {
        synchronized (lock) {
            try {
                return resolveArtifactsInner(artifactIds);
            } catch (Exception e) {
                throw new FeatureManagementException("Could not resolve artifacts.", e);
            }
        }
    }

    private List<IPath> resolveArtifactsInner(final List<String> artifactIds) throws Exception {
        final String scope = JavaScopes.RUNTIME;
        CollectRequest collectRequest = new CollectRequest();

        for (String artifactId : artifactIds) {
            collectRequest.addDependency(new Dependency(new DefaultArtifact(artifactId), scope));
        }

        collectRequest.setRepositories(remoteRepositories);

        DependencyFilter classpathFilter = DependencyFilterUtils.classpathFilter(scope);
        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFilter);

        List<ArtifactResult> artifactResults =
                repositorySystem.resolveDependencies(session, dependencyRequest).getArtifactResults();

        List<IPath> paths = new ArrayList<>(artifactResults.size());

        for (ArtifactResult result : artifactResults) {
            paths.add(new Path(result.getArtifact().getFile()));
        }

        return paths;
    }
}
