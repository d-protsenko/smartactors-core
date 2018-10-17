package info.smart_tools.smartactors.version_management.versioned_map_router_decorator;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Test for {@link VersionedRouterDecorator}.
 */
public class VersionedTaskQueueDecoratorTest {
    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrowWhenUnderlyingQueueIsNull() {
        assertNotNull(new VersionedRouterDecorator(null));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void Should_takeNotBeSupported()
            throws Exception {
        IQueue underlying = mock(IQueue.class);
        IQueue queue = new VersionedRouterDecorator(underlying);
        queue.take();
    }

    @Test
    public void Should_NonBlockingQueueCallMethodsOfUnderlyingQueue()
            throws Exception {
        IQueue underlying = mock(IQueue.class);
        Object object1 = new Object(), object2 = new Object(), object3 = new Object();

        when(underlying.tryTake()).thenReturn(object3);

        IQueue queue = new VersionedRouterDecorator(underlying);

        queue.put(object1);

        verify(underlying).put(same(object1));

        assertSame(object3, queue.tryTake());
    }

    @Test
    public void Should_CallCallbacksWhenNewItemAdded()
            throws Exception {
        IQueue underlying = mock(IQueue.class);

        Runnable callback1 = mock(Runnable.class), callback2 = mock(Runnable.class);

        //when(underlying.isEmpty()).thenReturn(true);

        IQueue queue = new VersionedRouterDecorator(underlying);

        queue.addNewItemCallback(callback1);
        queue.addNewItemCallback(callback2);

        verifyZeroInteractions(callback1, callback2);

        queue.put(new Object());

        verify(callback1).run();
        verify(callback2).run();
    }

    @Test
    public void Should_callCallbackImmediately_When_thereAlreadyAreElementsInQueue() {
        IQueue underlying = mock(IQueue.class);

        Runnable callback = mock(Runnable.class);

        //when(underlying.isEmpty()).thenReturn(false);

        IQueue queue = new VersionedRouterDecorator(underlying);

        queue.addNewItemCallback(callback);

        verify(callback).run();
    }

    @Test
    public void Should_notCallCallbackRemoved_When_itIsRemoved()
            throws Exception {
        IQueue underlying = mock(IQueue.class);

        Runnable callback = mock(Runnable.class);

        //when(underlying.isEmpty()).thenReturn(true);

        IQueue queue = new VersionedRouterDecorator(underlying);

        queue.addNewItemCallback(callback);
        queue.removeNewItemCallback(callback);

        queue.put(new Object());

        verify(callback, never()).run();
    }
}