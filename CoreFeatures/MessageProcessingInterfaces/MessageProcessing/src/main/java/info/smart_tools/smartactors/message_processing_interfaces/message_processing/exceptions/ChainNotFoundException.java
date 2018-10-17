package info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions;

import java.text.MessageFormat;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;

/**
 * Exception thrown by {@link IMessageProcessingSequence#callChain(Object)} when there
 * is no chain associated with given identifier.
 */
public class ChainNotFoundException extends Exception {
    /**
     * The constructor.
     *
     * @param chainId    identifier of the chain.
     */
    public ChainNotFoundException(final Object chainId) {
        super(MessageFormat.format("Chain ''{0}'' not found.", chainId));
    }

    /**
     * The constructor.
     *
     * @param chainId    identifier of the chain.
     */
    public ChainNotFoundException(final Object chainId, Throwable e) {
        super(MessageFormat.format("Chain ''{0}'' not found.", chainId), e);
    }
}
