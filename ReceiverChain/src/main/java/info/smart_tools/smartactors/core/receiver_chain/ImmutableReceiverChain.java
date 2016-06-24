package info.smart_tools.smartactors.core.receiver_chain;

import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;

/**
 * Basic implementation of {@link IReceiverChain} -- immutable sequence of receivers.
 */
public class ImmutableReceiverChain implements IReceiverChain {
    private final String name;
    private final IMessageReceiver[] receivers;
    private final IReceiverChain exceptionalReceiverChain;

    /**
     * The constructor.
     *
     * @param name                        name of the chain
     * @param receivers                   sequence (array) of receivers
     * @param exceptionalReceiverChain    chain that should be returned by {@link IReceiverChain#getExceptionalChain(Throwable)}
     *                                    on any exception
     * @throws InvalidArgumentException if name is {@code null}
     * @throws InvalidArgumentException if receivers is {@code null}
     */
    public ImmutableReceiverChain(final String name, final IMessageReceiver[] receivers, final IReceiverChain exceptionalReceiverChain)
            throws InvalidArgumentException {
        if (null == name) {
            throw new InvalidArgumentException("Chain name should not be null.");
        }

        if (null == receivers) {
            throw new InvalidArgumentException("Chain receivers list should not be null.");
        }

        this.name = name;
        this.receivers = receivers;
        this.exceptionalReceiverChain = exceptionalReceiverChain;
    }

    @Override
    public IMessageReceiver get(final int index) {
        if (index < 0 || index >= receivers.length) {
            return null;
        }

        return receivers[index];
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IReceiverChain getExceptionalChain(final Throwable exception) {
        return exceptionalReceiverChain;
    }
}
