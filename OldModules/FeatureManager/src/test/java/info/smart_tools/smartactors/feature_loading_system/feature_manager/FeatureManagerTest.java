package info.smart_tools.smartactors.feature_loading_system.feature_manager;

import info.smart_tools.smartactors.feature_loading_system.interfaces.ifeature_manager.IFeature;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ifeature_manager.exception.FeatureManagementException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ifilesystem_tracker.IFilesystemTracker;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link FeatureManager}.
 */
public class FeatureManagerTest {
    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_filesystemTrackerIsNull()
            throws Exception {
        new FeatureManager().newFeature("bug", null);
    }

    @Test(expected = FeatureManagementException.class)
    public void Should_newFeatureThrow_When_FeatureConstructorThrows()
            throws Exception {
        new FeatureManager().newFeature(null, mock(IFilesystemTracker.class));
    }

    @Test
    public void Should_createNewFeatures()
            throws Exception {
        IFeature feature = new FeatureManager().newFeature("bug", mock(IFilesystemTracker.class));

        assertNotNull(feature);
        assertTrue(feature instanceof Feature);
    }
}