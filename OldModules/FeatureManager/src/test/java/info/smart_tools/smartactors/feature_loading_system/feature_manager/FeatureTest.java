package info.smart_tools.smartactors.feature_loading_system.feature_manager;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.ipath.IPath;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ifeature_manager.exception.FeatureManagementException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ifilesystem_tracker.IFilesystemTracker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.BlockingDeque;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link Feature}.
 */
public class FeatureTest {
    private IFilesystemTracker filesystemTrackerMock;
    private BlockingDeque<ExecutionPair> queue;
    private ArgumentCaptor<IAction> trackerActionCaptor;

    @Before
    public void setUp()
            throws Exception {
        filesystemTrackerMock = mock(IFilesystemTracker.class);
        queue = mock(BlockingDeque.class);
        trackerActionCaptor = ArgumentCaptor.forClass(IAction.class);
        doNothing().when(queue).put(any(ExecutionPair.class));
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_theNameIsNull()
            throws Exception {
        new Feature(null, filesystemTrackerMock, queue);
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_theFilesystemTrackerIsNull()
            throws Exception {
        new Feature("bug", null, queue);
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_theQueueIsNull()
            throws Exception {
        new Feature("bug", filesystemTrackerMock, null);
    }

    @Test(expected = FeatureManagementException.class)
    public void Should_listenThrow_When_NoFilesSpecified()
            throws Exception {
        Feature feature = new Feature("bug", filesystemTrackerMock, queue);
        feature.listen();
    }

    @Test(expected = FeatureManagementException.class)
    public void Should_listenThrow_When_CalledTwice()
            throws Exception {
        Feature feature = new Feature("bug", filesystemTrackerMock, queue);

        feature.requireFile("bug-feature.jar");

        try {
            feature.listen();
        } catch (FeatureManagementException e) {
            fail("Should not throw yet.");
        }

        feature.listen();
    }

    @Test(expected = FeatureManagementException.class)
    public void Should_requireFileThrow_When_CalledAfter_listen()
            throws Exception {
        Feature feature = new Feature("bug", filesystemTrackerMock, queue);

        feature.requireFile("bug-feature.jar");

        try {
            feature.listen();
        } catch (FeatureManagementException e) {
            fail("Should not throw yet.");
        }

        feature.requireFile("nice-feature.txt");
    }

    @Test
    public void Should_getNameReturnNameGivenOnInitialization()
            throws Exception {
        Feature feature = new Feature("bug", filesystemTrackerMock, queue);

        assertEquals("bug", feature.getName());
    }

//    @Test
//    public void Should_subscribeForNotificationsFromFileSystemTracker_When_listenCalled()
//            throws Exception {
//        IAction actionMock1 = mock(IAction.class), actionMock2 = mock(IAction.class), actionMock3 = mock(IAction.class);
//        ActionExecuteException exceptionMock1 = mock(ActionExecuteException.class), exceptionMock2 = mock(ActionExecuteException.class);
//        IPath fileMock1 = mock(IPath.class), fileMock2 = mock(IPath.class), fileMock3 = mock(IPath.class);
//
//        doThrow(exceptionMock1).when(actionMock1).execute(any());
//        doThrow(exceptionMock2).when(actionMock2).execute(any());
//        when(fileMock1.getPath()).thenReturn("dir/bug-feature-1.jar");
//        when(fileMock2.getPath()).thenReturn("dir/bug-feature-2.jar");
//        when(fileMock3.getPath()).thenReturn("dir/nice-feature.txt");
//
//        Feature feature = new Feature("bug", filesystemTrackerMock, queue);
//
//        feature.requireFile("bug-feature-1.jar");
//        feature.requireFile("bug-feature-2.jar");
//
//        feature.whenPresent(actionMock1);
//        feature.whenPresent(actionMock2);
//        feature.whenPresent(actionMock3);
//
//        feature.listen();
//
//        verify(filesystemTrackerMock).addFileHandler(trackerActionCaptor.capture());
//
//        verifyZeroInteractions(actionMock1, actionMock2, actionMock3);
//
//        trackerActionCaptor.getValue().execute(fileMock1);
//        trackerActionCaptor.getValue().execute(fileMock3);
//
//        try {
//            trackerActionCaptor.getValue().execute(fileMock2);
//            fail();
//        } catch (ActionExecuteException e) {
//            assertSame(exceptionMock1, e.getCause());
//            assertEquals(e.getSuppressed().length, 1);
//            assertSame(exceptionMock2, e.getSuppressed()[0]);
//        }
//
//        verify(actionMock1).execute(any());
//        verify(actionMock2).execute(any());
//        verify(actionMock3).execute(any());
//        verify(filesystemTrackerMock).removeFileHandler(same(trackerActionCaptor.getValue()));
//        verify(queue).put(any(ExecutionPair.class));
//    }

    @Test
    public void Should_executeActionImmediately_When_filesAreAlreadyPresent()
            throws Exception {
        IPath fileMock = mock(IPath.class);
        IAction actionMock = mock(IAction.class);

        when(fileMock.getPath()).thenReturn("dir/bug-feature.jar");

        Feature feature = new Feature("bug", filesystemTrackerMock, queue);

        feature.requireFile("bug-feature.jar");

        feature.listen();

        verify(filesystemTrackerMock).addFileHandler(trackerActionCaptor.capture());
        trackerActionCaptor.getValue().execute(fileMock);

        feature.whenPresent(actionMock);

//        verify(actionMock).execute(any());
        verify(queue).put(any(ExecutionPair.class));
    }

//    @Test
//    public void Should_wrapException_When_filesAreAlreadyPresent_And_ImmediatelyExecutedActionThrows()
//            throws Exception {
//        IPath fileMock = mock(IPath.class);
//        IAction actionMock = mock(IAction.class);
//        ActionExecuteException exceptionMock = mock(ActionExecuteException.class);
//
//        when(fileMock.getPath()).thenReturn("dir/bug-feature.jar");
//        doThrow(exceptionMock).when(actionMock).execute(any());
//
//        Feature feature = new Feature("bug", filesystemTrackerMock, queue);
//
//        feature.requireFile("bug-feature.jar");
//
//        feature.listen();
//
//        verify(filesystemTrackerMock).addFileHandler(trackerActionCaptor.capture());
//        trackerActionCaptor.getValue().execute(fileMock);
//
//        try {
//            feature.whenPresent(actionMock);
//            fail();
//        } catch (FeatureManagementException e) {
//            assertSame(exceptionMock, e.getCause());
//        }
//
////        verify(actionMock).execute(any());
//        verify(queue).put(any(ExecutionPair.class));
//    }
}
