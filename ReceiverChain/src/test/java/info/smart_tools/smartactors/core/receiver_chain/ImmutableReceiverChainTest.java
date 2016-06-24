package info.smart_tools.smartactors.core.receiver_chain;

import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
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
        assertNotNull(new ImmutableReceiverChain(null, new IMessageReceiver[0], new IObject[0], null));
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_invalidArgumentsListPassed()
            throws Exception {
        assertNotNull(new ImmutableReceiverChain("theChain", new IMessageReceiver[0], null, null));
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_numberOfReceiversDoesNotMatchNumberOfArgumentsObjects()
            throws Exception {
        new ImmutableReceiverChain("theChain", new IMessageReceiver[1], new IObject[0], null);
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_invalidReceiversListPassed()
            throws Exception {
        assertNotNull(new ImmutableReceiverChain("theChain", null, new IObject[0], null));
    }

    @Test
    public void Should_beConstructedWithNoExceptionalChain()
            throws Exception {
        IMessageReceiver[] receivers = new IMessageReceiver[0];
        IReceiverChain chain = new ImmutableReceiverChain("theChain", receivers, new IObject[0], null);

        assertEquals("theChain", chain.getName());
    }

    @Test
    public void Should_beConstructedWithExceptionalChain()
            throws Exception {
        IMessageReceiver[] receivers = new IMessageReceiver[0];
        IReceiverChain exceptionalChain = mock(IReceiverChain.class);

        IReceiverChain chain = new ImmutableReceiverChain("theChain", receivers, new IObject[0], exceptionalChain);

        assertEquals("theChain", chain.getName());
        assertSame(exceptionalChain, chain.getExceptionalChain(mock(Throwable.class)));
    }

    @Test
    public void Should_get_returnMessageReceiversAndArgumentObjects()
            throws Exception {
        IMessageReceiver[] receivers = new IMessageReceiver[] {
                mock(IMessageReceiver.class),
                mock(IMessageReceiver.class)};
        IObject[] arguments = new IObject[] {
                mock(IObject.class),
                mock(IObject.class)};

        IReceiverChain chain = new ImmutableReceiverChain("theChain", receivers, arguments, null);

        assertSame(receivers[0], chain.get(0));
        assertSame(arguments[0], chain.getArguments(0));
        assertSame(receivers[1], chain.get(1));
        assertSame(arguments[1], chain.getArguments(1));
        assertNull(chain.get(2));
        assertNull(chain.getArguments(2));
    }
}
