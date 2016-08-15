package info.smart_tools.smartactors.core.chain_testing.checkers;

import info.smart_tools.smartactors.core.chain_testing.exceptions.AssertionFailureException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;

/**
 *
 */
public interface TestResultChecker {
    /**
     * Check if the test was completed as expected.
     *
     * @param mp the message processor processed the test message
     * @param exc the exception occurred processing message or {@code null} if chain completed successful
     * @throws AssertionFailureException if test is completed not as expected
     */
    void check(final IMessageProcessor mp, final Throwable exc) throws AssertionFailureException;

    /**
     * Get arguments object that should be associated with the step of {@link info.smart_tools.smartactors.core.chain_testing.MainTestChain}
     * that will be reached in case of successful completion of chain.
     *
     * @return the arguments object
     */
    IObject getSuccessfulReceiverArguments();
}
