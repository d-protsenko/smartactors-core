package info.smart_tools.smartactors.core.blocking_queue;

import info.smart_tools.smartactors.core.iqueue.IQueue;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test for {@link BlockingQueue}
 */
public class BlockingQueueTest {
    @Test
    public void Should_BlockingQueueCallMethodsOfUnderlyingQueue()
            throws Exception {
        java.util.concurrent.BlockingQueue underlying = mock(java.util.concurrent.BlockingQueue.class);
        Object object1 = new Object(), object2 = new Object();

        when(underlying.take()).thenReturn(object2);

        IQueue queue = new BlockingQueue<>(underlying);

        queue.put(object1);

        verify(underlying).put(same(object1));

        assertSame(object2, queue.take());
    }
}
