package info.smart_tools.smartactors.core.feature_loader;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.IBiAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.interfaces.ipath.IPath;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.reset;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Test for {@link FeatureStatusImpl}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({IOC.class, Keys.class})
public class FeatureStatusImplTest {
    private IKey taskQueueKey = mock(IKey.class);
    private IQueue<ITask> taskQueueMock;
    private IBiAction<IObject, IPath> loadActionMock;
    private IPath pathMock1;
    private IObject configMock;
    private IAction<Throwable> callbackMock1;
    private IAction<Throwable> callbackMock2;
    private IAction<Throwable> callbackMock3;
    private IAction<Throwable> callbackMock4;
    private FeatureStatusImpl dependencyMock1;

    private ITask expectEnqueued()
            throws Exception {
        ArgumentCaptor<ITask> captor = ArgumentCaptor.forClass(ITask.class);
        verify(taskQueueMock).put(captor.capture());
        reset(taskQueueMock);
        return captor.getValue();
    }

    private IAction<Throwable> expectCallback(final FeatureStatusImpl dependency)
            throws Exception {
        ArgumentCaptor<IAction> captor = ArgumentCaptor.forClass(IAction.class);
        verify(dependency).whenDone(captor.capture());
        reset(dependency);
        return captor.getValue();
    }

    @Before
    public void setUp()
            throws Exception {
        mockStatic(IOC.class, Keys.class);

        taskQueueMock = mock(IQueue.class);
        when(Keys.getOrAdd(eq("task_queue"))).thenReturn(taskQueueKey);
        when(IOC.resolve(taskQueueKey)).thenReturn(taskQueueMock);

        loadActionMock = mock(IBiAction.class);

        pathMock1 = mock(IPath.class);
        configMock = mock(IObject.class);

        callbackMock1 = mock(IAction.class);
        callbackMock2 = mock(IAction.class);
        callbackMock3 = mock(IAction.class);
        callbackMock4 = mock(IAction.class);

        dependencyMock1 = mock(FeatureStatusImpl.class);
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_idIsNull()
            throws Exception {
        assertNotNull(new FeatureStatusImpl(null, loadActionMock));
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_loadActionIsNull()
            throws Exception {
        assertNotNull(new FeatureStatusImpl("feature", null));
    }

    @Test
    public void Should_beCreated()
            throws Exception {
        FeatureStatusImpl status = new FeatureStatusImpl("feature", loadActionMock);

        assertFalse(status.isInitialized());
        assertFalse(status.isFailed());
        assertFalse(status.isLoaded());
        assertEquals("feature", status.getId());
        assertNull(status.getPath());
        assertEquals(0, status.getDependencies().size());

        status.init(pathMock1, configMock);

        assertTrue(status.isInitialized());
        assertSame(pathMock1, status.getPath());
    }

    @Test
    public void Should_executeLoadActionUsingTskQueue()
            throws Exception {
        FeatureStatusImpl status = new FeatureStatusImpl("feature", loadActionMock);
        status.whenDone(callbackMock1);

        status.init(pathMock1, configMock);

        status.load();

        expectEnqueued().execute();

        verify(loadActionMock).execute(configMock, pathMock1);
        verify(callbackMock1).execute(same(null));

        assertTrue(status.isLoaded());
        assertFalse(status.isFailed());
    }

    @Test
    public void Should_executeAllCallbacksEvenSomeOfThemThrowOrFeatureIsAlreadyLoaded()
            throws Exception {
        FeatureStatusImpl status = new FeatureStatusImpl("feature", loadActionMock);
        status.whenDone(callbackMock1);
        status.whenDone(callbackMock2);
        status.whenDone(callbackMock3);

        doThrow(new ActionExecuteException("")).when(callbackMock1).execute(same(null));
        doThrow(new ActionExecuteException("")).when(callbackMock2).execute(same(null));

        status.init(pathMock1, configMock);

        status.load();

        expectEnqueued().execute();

        verify(loadActionMock).execute(configMock, pathMock1);
        verify(callbackMock1).execute(same(null));
        verify(callbackMock2).execute(same(null));
        verify(callbackMock3).execute(same(null));

        assertTrue(status.isLoaded());
        assertFalse(status.isFailed());

        status.whenDone(callbackMock4);
        verify(callbackMock4).execute(null);
    }

    @Test
    public void Should_awaitForDependenciesToLoad()
            throws Exception {
        FeatureStatusImpl status = new FeatureStatusImpl("feature", loadActionMock);
        status.whenDone(callbackMock1);

        status.addDependency(dependencyMock1);
        IAction<Throwable> cb1 = expectCallback(dependencyMock1);

        status.init(pathMock1, configMock);

        status.load();

        verify(taskQueueMock, times(0)).put(any());

        cb1.execute(null);

        expectEnqueued().execute();

        verify(callbackMock1).execute(same(null));

        assertTrue(status.isLoaded());
        assertFalse(status.isFailed());
    }

    @Test
    public void Should_failWhenDependencyFails()
            throws Exception {
        Exception err = new Exception();
        ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);

        FeatureStatusImpl status = new FeatureStatusImpl("feature", loadActionMock);

        doThrow(new ActionExecuteException("")).when(callbackMock1).execute(any());

        status.whenDone(callbackMock1);
        status.whenDone(callbackMock2);

        status.addDependency(dependencyMock1);
        IAction<Throwable> cb1 = expectCallback(dependencyMock1);

        status.init(pathMock1, configMock);

        status.load();

        verify(taskQueueMock, times(0)).put(any());

        cb1.execute(err);

        verify(callbackMock1).execute(captor.capture());
        verify(callbackMock2).execute(same(captor.getValue()));

        assertSame(err, captor.getValue().getCause());
        assertEquals(1, captor.getValue().getSuppressed().length);
    }
}
