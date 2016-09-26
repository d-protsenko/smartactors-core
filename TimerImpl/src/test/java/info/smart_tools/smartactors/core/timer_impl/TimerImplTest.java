package info.smart_tools.smartactors.core.timer_impl;

import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itimer.ITimer;
import info.smart_tools.smartactors.core.itimer.exceptions.TaskScheduleException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.verification.Timeout;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Timer;
import java.util.TimerTask;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test for {@link TimerImpl}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({IOC.class, Keys.class})
public class TimerImplTest {
    private static Timer realTimer;
    private Timer timerMock;
    private ITask taskMock;
    private IQueue<ITask> taskQueueMock;
    private IKey taskQueueKey;

    @BeforeClass
    public static void setUpClass()
            throws Exception {
        realTimer = new Timer(true);
    }

    @AfterClass
    public static void shutDownClass()
            throws Exception {
        realTimer.cancel();
    }

    @Before
    public void setUp()
            throws Exception {
        mockStatic(IOC.class, Keys.class);

        timerMock = mock(Timer.class);
        taskMock = mock(ITask.class);
        taskQueueMock = mock(IQueue.class);
        taskQueueKey =mock(IKey.class);

        when(Keys.getOrAdd(eq("task_queue"))).thenReturn(taskQueueKey);
        when(IOC.resolve(same(taskQueueKey))).thenReturn(taskQueueMock);
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrowWhenTimerIsNull()
            throws Exception {
        assertNotNull(new TimerImpl(null));
    }

    @Test
    public void Should_resolveQueueSchedulingTask()
            throws Exception {
        ITimer timer = new TimerImpl(timerMock);

        long tm = System.currentTimeMillis() + 1337;

        timer.schedule(taskMock, tm);

        verifyStatic();
        IOC.resolve(same(taskQueueKey));

        ArgumentCaptor<TimerTask> timerTaskArgumentCaptor = ArgumentCaptor.forClass(TimerTask.class);
        ArgumentCaptor<Long> timeArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        verify(timerMock).schedule(timerTaskArgumentCaptor.capture(), timeArgumentCaptor.capture());
        assertTrue(1000L > Math.abs(1337 - timeArgumentCaptor.getValue()));

        timerTaskArgumentCaptor.getValue().run();

        verify(taskQueueMock).put(same(taskMock));
    }

    @Test
    public void Should_javaTimerTaskImplementationHandleInterruptedException()
            throws Exception {
        doThrow(InterruptedException.class).when(taskQueueMock).put(same(taskMock));
        ITimer timer = new TimerImpl(timerMock);

        timer.schedule(taskMock, 0);

        ArgumentCaptor<TimerTask> timerTaskArgumentCaptor = ArgumentCaptor.forClass(TimerTask.class);
        verify(timerMock).schedule(timerTaskArgumentCaptor.capture(), anyLong());

        assertFalse(Thread.interrupted());
        timerTaskArgumentCaptor.getValue().run();
        assertTrue(Thread.interrupted());
    }

    @Test(expected = TaskScheduleException.class)
    public void Should_throwWhenCannotResolveQueue()
            throws Exception {
        when(IOC.resolve(same(taskQueueKey))).thenThrow(ResolutionException.class);
        ITimer timer = new TimerImpl(timerMock);

        timer.schedule(taskMock, 0);
    }

    @Test
    public void Should_cancelCancelTheTask()
            throws Exception {
        ITimer timer = new TimerImpl(realTimer);
        timer.schedule(taskMock, System.currentTimeMillis() + 500).cancel();
        Thread.sleep(500);
        verify(taskQueueMock, times(0)).put(any());
    }
}
