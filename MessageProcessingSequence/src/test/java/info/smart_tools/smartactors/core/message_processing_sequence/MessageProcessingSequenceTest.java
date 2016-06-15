package info.smart_tools.smartactors.core.message_processing_sequence;

import info.smart_tools.smartactors.core.imessage_processing_sequence.IMessageProcessingSequence;
import info.smart_tools.smartactors.core.imessage_processing_sequence.exceptions.NestedChainStackOverflowException;
import info.smart_tools.smartactors.core.imessage_processing_sequence.exceptions.NoExceptionHandleChainException;
import info.smart_tools.smartactors.core.imessage_receiver.IMessageReceiver;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ireceiver_chain.IReceiverChain;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link MessageProcessingSequence}.
 */
public class MessageProcessingSequenceTest {
    private IReceiverChain mainChainMock;
    private IMessageReceiver[] messageReceiverMocks;

    @Before
    public void setUp()
            throws Exception {
        mainChainMock = mock(IReceiverChain.class);
        messageReceiverMocks = new IMessageReceiver[10];

        for (int i = 0; i < messageReceiverMocks.length; i++) {
            messageReceiverMocks[i] = mock(IMessageReceiver.class);
        }
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_invalidStackDepthGiven()
            throws Exception {
        assertNotNull(new MessageProcessingSequence(0, mock(IReceiverChain.class)));
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_nullMainChainGiven()
            throws Exception {
        assertNotNull(new MessageProcessingSequence(1, null));
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_mainChainContainsNoReceivers()
            throws Exception {
        when(mainChainMock.get(eq(0))).thenReturn(null);

        assertNotNull(new MessageProcessingSequence(1, mainChainMock));
    }

    @Test(expected = NestedChainStackOverflowException.class)
    public void Should_throw_When_stackOverflowOccurs()
            throws Exception {
        when(mainChainMock.get(eq(0))).thenReturn(messageReceiverMocks[0]);

        IMessageProcessingSequence messageProcessingSequence = new MessageProcessingSequence(4, mainChainMock);

        try {
            messageProcessingSequence.callChain(mainChainMock);
            messageProcessingSequence.callChain(mainChainMock);
            messageProcessingSequence.callChain(mainChainMock);
        } catch (NestedChainStackOverflowException e) {
            fail();
        }

        messageProcessingSequence.callChain(mainChainMock);
    }

    @Test
    public void Should_moveOverAllReceiversInAllNestedChains()
            throws Exception {
        IReceiverChain chainMock1 = mock(IReceiverChain.class);
        IReceiverChain chainMock2 = mock(IReceiverChain.class);
        IReceiverChain chainMock3 = mock(IReceiverChain.class);

        when(mainChainMock.get(eq(0))).thenReturn(messageReceiverMocks[0]);
        when(mainChainMock.get(eq(1))).thenReturn(messageReceiverMocks[1]);
        when(mainChainMock.get(eq(2))).thenReturn(messageReceiverMocks[2]);

        when(chainMock1.get(eq(0))).thenReturn(messageReceiverMocks[3]);
        when(chainMock1.get(eq(1))).thenReturn(messageReceiverMocks[4]);

        when(chainMock2.get(eq(0))).thenReturn(messageReceiverMocks[5]);
        when(chainMock2.get(eq(1))).thenReturn(messageReceiverMocks[6]);

        when(chainMock3.get(eq(0))).thenReturn(messageReceiverMocks[7]);
        when(chainMock3.get(eq(1))).thenReturn(messageReceiverMocks[8]);

        IMessageProcessingSequence messageProcessingSequence = new MessageProcessingSequence(4, mainChainMock);

        assertSame(messageReceiverMocks[0], messageProcessingSequence.getCurrentReceiver());
        assertSame(messageReceiverMocks[0], messageProcessingSequence.getCurrentReceiver());
        assertTrue(messageProcessingSequence.next());
        assertSame(messageReceiverMocks[1], messageProcessingSequence.getCurrentReceiver());

        messageProcessingSequence.callChain(chainMock1);

        assertTrue(messageProcessingSequence.next());
        assertSame(messageReceiverMocks[3], messageProcessingSequence.getCurrentReceiver());

        messageProcessingSequence.callChain(chainMock2);

        assertTrue(messageProcessingSequence.next());
        assertSame(messageReceiverMocks[5], messageProcessingSequence.getCurrentReceiver());
        assertTrue(messageProcessingSequence.next());
        assertSame(messageReceiverMocks[6], messageProcessingSequence.getCurrentReceiver());

        messageProcessingSequence.callChain(chainMock3);

        assertTrue(messageProcessingSequence.next());
        assertSame(messageReceiverMocks[7], messageProcessingSequence.getCurrentReceiver());
        assertTrue(messageProcessingSequence.next());
        assertSame(messageReceiverMocks[8], messageProcessingSequence.getCurrentReceiver());

        assertTrue(messageProcessingSequence.next());
        assertSame(messageReceiverMocks[4], messageProcessingSequence.getCurrentReceiver());

        assertTrue(messageProcessingSequence.next());
        assertSame(messageReceiverMocks[2], messageProcessingSequence.getCurrentReceiver());

        assertFalse(messageProcessingSequence.next());
        assertNull(messageProcessingSequence.getCurrentReceiver());
    }

    @Test
    public void Should_catchException_searchForExceptionalChainAndStartItsExecutionIfFound()
            throws Exception {
        IReceiverChain chainMock1 = mock(IReceiverChain.class);
        IReceiverChain chainMock2 = mock(IReceiverChain.class);
        IReceiverChain exceptionalChainMock = mock(IReceiverChain.class);
        Throwable exception = mock(Throwable.class);

        when(chainMock1.getExceptionalChain(same(exception))).thenReturn(exceptionalChainMock);

        when(exceptionalChainMock.get(eq(0))).thenReturn(messageReceiverMocks[0]);

        when(chainMock1.get(eq(0))).thenReturn(messageReceiverMocks[1]);
        when(chainMock2.get(eq(0))).thenReturn(messageReceiverMocks[2]);

        when(mainChainMock.get(0)).thenReturn(messageReceiverMocks[3]);
        when(mainChainMock.get(1)).thenReturn(messageReceiverMocks[4]);

        IMessageProcessingSequence messageProcessingSequence = new MessageProcessingSequence(4, mainChainMock);

        assertSame(messageReceiverMocks[3], messageProcessingSequence.getCurrentReceiver());

        messageProcessingSequence.callChain(chainMock1);
        messageProcessingSequence.callChain(chainMock2);

        assertTrue(messageProcessingSequence.next());

        assertSame(messageReceiverMocks[2], messageProcessingSequence.getCurrentReceiver());

        messageProcessingSequence.catchException(exception);

        assertTrue(messageProcessingSequence.next());
        assertSame(messageReceiverMocks[0], messageProcessingSequence.getCurrentReceiver());

        assertTrue(messageProcessingSequence.next());
        assertSame(messageReceiverMocks[4], messageProcessingSequence.getCurrentReceiver());
    }

    @Test(expected = NoExceptionHandleChainException.class)
    public void Should_throwWhenNoChainFoundForException()
            throws Exception {
        Throwable exception = mock(Throwable.class);

        when(mainChainMock.getExceptionalChain(same(exception))).thenReturn(null);
        when(mainChainMock.get(eq(0))).thenReturn(messageReceiverMocks[0]);

        IMessageProcessingSequence sequence = new MessageProcessingSequence(1, mainChainMock);

        sequence.catchException(exception);
    }
}
