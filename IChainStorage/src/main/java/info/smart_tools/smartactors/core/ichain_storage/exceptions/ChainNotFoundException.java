package info.smart_tools.smartactors.core.ichain_storage.exceptions;

import java.text.MessageFormat;

/**
 * Exception thrown by {@link info.smart_tools.smartactors.core.ichain_storage.IChainStorage#resolve(Object)} when there
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
}
