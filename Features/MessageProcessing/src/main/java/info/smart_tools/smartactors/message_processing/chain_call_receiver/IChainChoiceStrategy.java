package info.smart_tools.smartactors.message_processing.chain_call_receiver;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;

/**
 * Strategy used by {@link ChainCallReceiver} to choose what chain to call.
 */
public interface IChainChoiceStrategy {
    /**
     * Choose a chain to be called on the message being processed by given message processor.
     *
     * @param messageProcessor    message processor processing the message
     * @return name of the chain to be called for the message
     */
    Object chooseChain(IMessageProcessor messageProcessor)
            throws InvalidArgumentException, ReadValueException;
}
