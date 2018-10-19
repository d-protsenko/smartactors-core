package info.smart_tools.smartactors.message_processing.chain_call_receiver;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.ChainChoiceException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.MessageReceiveException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.NestedChainStackOverflowException;
import info.smart_tools.smartactors.scope.iscope_provider_container.exception.ScopeProviderException;

/**
 * Receiver that calls {@link IReceiverChain} chosen by a {@link IChainChoiceStrategy} on a message.
 */
public class ChainCallReceiver implements IMessageReceiver {


    private IChainChoiceStrategy chainChoiceStrategy;

    /**
     * The constructor.
     *
     * @param chainChoiceStrategy           strategy to use
     * @throws InvalidArgumentException     if storage is {@code null}
     * @throws InvalidArgumentException     if strategy is {@code null}
     */
    public ChainCallReceiver(final IChainChoiceStrategy chainChoiceStrategy)
            throws InvalidArgumentException {
        if (null == chainChoiceStrategy) {
            throw new InvalidArgumentException("Strategy should not be null.");
        }

        this.chainChoiceStrategy = chainChoiceStrategy;
    }

    @Override
    public void receive(final IMessageProcessor processor)
            throws MessageReceiveException {
        try {
            Object chainName = chainChoiceStrategy.chooseChain(processor);
            processor.getSequence().callChainSecurely(chainName, processor);
        } catch (ChainChoiceException | ChainNotFoundException | NestedChainStackOverflowException |
                ResolutionException | ReadValueException | InvalidArgumentException | ScopeProviderException e) {
            throw new MessageReceiveException("Could not call nested chain.", e);
        }
    }

    @Override
    public void dispose() {
    }
}
