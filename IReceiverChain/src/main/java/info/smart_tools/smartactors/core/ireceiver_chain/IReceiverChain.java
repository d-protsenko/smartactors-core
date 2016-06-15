package info.smart_tools.smartactors.core.ireceiver_chain;

import info.smart_tools.smartactors.core.imessage_receiver.IMessageReceiver;

/**
 * Chain of message receivers.
 */
public interface IReceiverChain {
    /**
     * Get {@link IMessageReceiver} at given index.
     *
     * @param index    index of the receiver in the chain
     * @return receiver at given index or {@code null} if there is no such receiver
     */
    IMessageReceiver get(int index);

    /**
     * Get name of this chain.
     *
     * @return name of this chain
     */
    String getName();

    /**
     * Get the chin of receivers that has to be executed if exception occurs during execution of this chain.
     *
     * @param exception the exception occurred
     * @return the chin of receivers that has to be executed if exception occurs during execution of this chain or
     *         {@code null} if no such chain defined for this one
     */
    IReceiverChain getExceptionalChain(Throwable exception);
}
