package info.smart_tools.smartactors.core.imessage_processing_sequence.exceptions;

import info.smart_tools.smartactors.core.ireceiver_chain.IReceiverChain;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for {@link NestedChainStackOverflowException}.
 */
public class NestedChainStackOverflowExceptionTest {
    @Test
    public void Should_getConstructedAndStoreAttributes()
            throws Exception {
        IReceiverChain[] receiverChainsStack = new IReceiverChain[0];
        int[] stepsStack = new int[0];
        NestedChainStackOverflowException exception = new NestedChainStackOverflowException(receiverChainsStack, stepsStack);

        assertSame(receiverChainsStack, exception.getChainsStack());
        assertSame(stepsStack, exception.getStepsStack());
    }
}
