package info.smart_tools.smartactors.core.dependency_resolving_feature_manager;

import info.smart_tools.smartactors.core.ifeature_manager.IFeature;
import info.smart_tools.smartactors.core.ifeature_manager.exception.FeatureManagementException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test for {@link DependencyResolvingFeatureManager}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({
    MavenRepositorySystemUtils.class,
})
public class DependencyResolvingFeatureManagerTest {
    private Map<String, String> remoteRepos;

    @Before
    public void setUp()
            throws Exception {
        remoteRepos = new HashMap<String, String>() {{
            put("local_repo", "file:///.m2/local");
            put("central", "http://repo1.maven.org/maven/");
        }};
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_localRepositoryPathIsNull()
            throws Exception {
        assertNotNull(new DependencyResolvingFeatureManager(null, remoteRepos));
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_remoteRepositoriesListIsNull()
            throws Exception {
        assertNotNull(new DependencyResolvingFeatureManager("/", null));
    }

    @Test(expected = FeatureManagementException.class)
    public void Should_throw_When_InvalidArgumentsPassedForFeatureCreation()
            throws Exception {
        assertNotNull(new DependencyResolvingFeatureManager("/", remoteRepos)
                .newFeature(null, null));
    }

    @Test
    public void Should_createFeatures()
            throws Exception {
        IFeature feature = new DependencyResolvingFeatureManager("/", remoteRepos)
                .newFeature("feature", null);

        assertNotNull(feature);
        assertTrue(feature instanceof Feature);
        assertEquals("feature", feature.getName());
    }
}
