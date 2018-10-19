package info.smart_tools.smartactors.core.dependency_resolving_feature_manager;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.interfaces.ipath.IPath;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ifeature_manager.IFeature;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ifeature_manager.exception.FeatureManagementException;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Test for {@link Feature}.
 */
public class FeatureTest {
    private DependencyResolvingFeatureManager managerMock;
    private IAction<Collection<IPath>> action1;
    private IAction<Collection<IPath>> action2;
    private IAction<Collection<IPath>> action3;
    private List<IPath> pathsMock;

    @Before
    public void setUp()
            throws Exception {
        managerMock = mock(DependencyResolvingFeatureManager.class);
        action1 = mock(IAction.class);
        action2 = mock(IAction.class);
        action3 = mock(IAction.class);
        pathsMock = mock(List.class);

        when(managerMock.resolveArtifacts(any())).thenReturn(pathsMock);
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_ManagerIsNull()
            throws Exception {
        assertNotNull(new Feature(null, "bug"));
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_NameIsNull()
            throws Exception {
        assertNotNull(new Feature(managerMock, null));
    }

    @Test
    public void Should_storeName()
            throws Exception {
        assertEquals("not_a_bug", new Feature(managerMock, "not_a_bug").getName());
    }

    @Test
    public void Should_callListenersWhenArtifactsResolved()
            throws Exception {
        IFeature feature = new Feature(managerMock, "feature");

        feature.whenPresent(action1);
        feature.whenPresent(action2);
        feature.whenPresent(action3);

        feature.requireFile("the_file");

        feature.listen();

        verify(action1).execute(same(pathsMock));
        verify(action2).execute(same(pathsMock));
        verify(action3).execute(same(pathsMock));
    }

    @Test
    public void Should_callListenerWhenArtifactsAreResolvedBeforeItsAddition()
            throws Exception {
        IFeature feature = new Feature(managerMock, "feature");

        feature.requireFile("the_file");
        feature.listen();
        feature.whenPresent(action1);

        verify(action1).execute(same(pathsMock));
    }

    @Test(expected = FeatureManagementException.class)
    public void Should_wrapException_When_ListenerAddedAfterResolutionThrows()
            throws Exception {
        doThrow(ActionExecuteException.class).when(action1).execute(same(pathsMock));

        IFeature feature = new Feature(managerMock, "feature");

        feature.requireFile("the_file");
        feature.listen();
        feature.whenPresent(action1);
    }

    @Test(expected = FeatureManagementException.class)
    public void Should_throw_When_requireFileCalledAfterArtifactsResolution()
            throws Exception {
        IFeature feature = new Feature(managerMock, "feature");

        feature.requireFile("the_file");
        feature.listen();

        feature.requireFile("the_late_file");
    }

    @Test(expected = FeatureManagementException.class)
    public void Should_throw_When_listenCalledAfterArtifactsResolution()
            throws Exception {
        IFeature feature = new Feature(managerMock, "feature");

        feature.requireFile("the_file");
        feature.listen();

        feature.listen();
    }

    @Test(expected = FeatureManagementException.class)
    public void Should_throw_When_listenCalledBeforeAnyArtifactsRequired()
            throws Exception {
        IFeature feature = new Feature(managerMock, "feature");

        feature.listen();
    }

    @Test
    public void Should_callAllListenersAndSuppressThrownExceptionsIfAnyListenerThrows()
            throws Exception {
        ActionExecuteException exception1 = new ActionExecuteException("e1");
        ActionExecuteException exception2 = new ActionExecuteException("e2");
        ActionExecuteException exception3 = new ActionExecuteException("e3");

        doThrow(exception1).when(action1).execute(same(pathsMock));
        doThrow(exception2).when(action2).execute(same(pathsMock));
        doThrow(exception3).when(action3).execute(same(pathsMock));

        IFeature feature = new Feature(managerMock, "feature");

        feature.requireFile("the_file");
        feature.whenPresent(action1);
        feature.whenPresent(action2);
        feature.whenPresent(action3);

        try {
            feature.listen();
            fail();
        } catch (FeatureManagementException e) {
            assertSame(exception1, e.getCause());
            assertEquals(2, exception1.getSuppressed().length);
            assertSame(exception2, exception1.getSuppressed()[0]);
            assertSame(exception3, exception1.getSuppressed()[1]);
        }
    }
}
