package info.smart_tools.smartactors.core.receiver_chain;

import info.smart_tools.smartactors.core.imessage_receiver.IMessageReceiver;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ireceiver_chain.IReceiverChain;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link ImmutableReceiverChain}.
 */
public class ImmutableReceiverChainTest {
    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_invalidNamePassed()
            throws Exception {
        assertNotNull(new ImmutableReceiverChain(null, new IMessageReceiver[0], null));
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_invalidReceiversListPassed()
            throws Exception {
        assertNotNull(new ImmutableReceiverChain("theChain", null, null));
    }

    @Test
    public void Should_beConstructedWithNoExceptionalChain()
            throws Exception {
        IMessageReceiver[] receivers = new IMessageReceiver[0];
        IReceiverChain chain = new ImmutableReceiverChain("theChain", receivers, null);

        assertEquals("theChain", chain.getName());
    }

    @Test
    public void Should_beConstructedWithExceptionalChain()
            throws Exception {
        IMessageReceiver[] receivers = new IMessageReceiver[0];
        IReceiverChain exceptionalChain = mock(IReceiverChain.class);

        IReceiverChain chain = new ImmutableReceiverChain("theChain", receivers, exceptionalChain);

        assertEquals("theChain", chain.getName());
        assertSame(exceptionalChain, chain.getExceptionalChain());
    }

    @Test
    public void Should_get_returnMessageReceivers()
            throws Exception {
        IMessageReceiver[] receivers = new IMessageReceiver[] {
                mock(IMessageReceiver.class),
                mock(IMessageReceiver.class)};

        IReceiverChain chain = new ImmutableReceiverChain("theChain", receivers, null);

        assertSame(receivers[0], chain.get(0));
        assertSame(receivers[1], chain.get(1));
        assertNull(chain.get(2));
    }
}