package info.smart_tools.smartactors.core.message_processing;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.message_processing.exceptions.NestedChainStackOverflowException;
import info.smart_tools.smartactors.core.message_processing.exceptions.NoExceptionHandleChainException;

/**
 * Object managing order of operations executed on a single message.
 *
 * May be used for processing o new messages when processing of current one is finished (see {@link #reset()}).
 *
 * Contains reference to a {@link IReceiverChain} (called main chain) used to process new messages. New chains may be
 * substituted using {@link #callChain(IReceiverChain)} method.
 */
public interface IMessageProcessingSequence {
    /**
     * Start processing of (new) message from first receiver of main chain.
     */
    void reset();

    /**
     * Switch current receiver reference to next receiver that should receive the message.
     *
     * @return {@code true} if there is at least one more receiver that should receive the message and {@code false} if
     *                      not
     */
    boolean next();

    /**
     * Get the next receiver that should receive the message.
     *
     * @return the receiver or {@code null} if message processing is finished
     */
    IMessageReceiver getCurrentReceiver();

    /**
     * Get the arguments object that should be passed to current receiver.
     *
     * @return the arguments objects or {@code null} if message processing is finished
     */
    IObject getCurrentReceiverArguments();

    /**
     * Interrupt execution of current chain by execution of a given one and when it is completed continue the previous.
     * Puts the new chain into a stack.
     *
     * @param chain    the {@link IReceiverChain} to call
     * @throws NestedChainStackOverflowException if overflow of nested chains stack occurs
     */
    void callChain(final IReceiverChain chain)
            throws NestedChainStackOverflowException;

    /**
     * Switch to the chain that should be executed when exception occurs.
     *
     * @param exception     the occurred exception
     * @throws NoExceptionHandleChainException when there is no such chain.
     */
    void catchException(final Throwable exception)
            throws NoExceptionHandleChainException;
}
