package info.smart_tools.smartactors.core.task_dispatcher;

import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.ithread_pool.IThread;
import info.smart_tools.smartactors.core.ithread_pool.IThreadPool;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Test for {@link ExecutionTask}
 */
public class ExecutionTaskTest {
    private TaskDispatcher dispatcherMock;
    private IQueue<ITask> queueMock;
    private IThreadPool threadPoolMock;

    @Before
    public void setUp()
            throws Exception {
        dispatcherMock = mock(TaskDispatcher.class);
        queueMock = (IQueue<ITask>) mock(IQueue.class);
        threadPoolMock = mock(IThreadPool.class);

        when(dispatcherMock.getTaskQueue()).thenReturn(queueMock);
        when(dispatcherMock.getThreadPool()).thenReturn(threadPoolMock);
    }

    @Test(timeout = 100L)
    public void Should_exit_When_thereIsNoTask()
            throws Exception {
        ExecutionTask executionTask = new ExecutionTask(dispatcherMock);

        when(queueMock.tryTake()).thenReturn(null);

        executionTask.execute();
    }

    @Test(timeout = 100L)
    public void Should_exit_When_threadIsInterrupted()
            throws Exception {
        ExecutionTask executionTask = new ExecutionTask(dispatcherMock);

        Thread.currentThread().interrupt();

        executionTask.execute();

        verify(queueMock, never()).tryTake();
    }

    @Test(timeout = 100L)
    public void Should_takeAndExecuteTasks()
            throws Exception {
        ITask taskMock = mock(ITask.class);

        when(queueMock.tryTake())
                .thenReturn(taskMock)
                .thenReturn(null);

        ExecutionTask executionTask = new ExecutionTask(dispatcherMock);

        executionTask.execute();

        verify(taskMock).execute();
    }

    @Test(timeout = 100L)
    public void Should_executeItselfOnNewThreadIfCanGetOne()
            throws Exception {
        ITask taskMock = mock(ITask.class);
        IThread threadMock = mock(IThread.class);

        when(queueMock.tryTake())
                .thenReturn(taskMock)
                .thenReturn(null);

        when(threadPoolMock.getThread())
                .thenReturn(threadMock)
                .thenReturn(null);

        ExecutionTask executionTask = new ExecutionTask(dispatcherMock);

        executionTask.execute();

        verify(taskMock).execute();
        verify(threadMock).execute(same(executionTask));
    }
}
