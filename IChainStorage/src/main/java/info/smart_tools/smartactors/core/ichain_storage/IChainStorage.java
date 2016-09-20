package info.smart_tools.smartactors.core.ichain_storage;

import info.smart_tools.smartactors.core.ichain_storage.exceptions.ChainCreationException;
import info.smart_tools.smartactors.core.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;

import java.util.List;

/**
 * Storage of {@link IReceiverChain}'s.
 */
public interface IChainStorage {
    /**
     * Create a chain from a description object and store it with given identifier.
     *
     * @param chainId        identifier to store chain with
     * @param description    object describing the chain
     * @throws ChainCreationException if any error occurs
     */
    void register(Object chainId, IObject description) throws ChainCreationException;

    /**
     * Find a chain associated with given identifier.
     *
     * @param chainId    identifier o a chain
     * @return the found chain
     * @throws ChainNotFoundException if there is no chain associated with given identifier
     */
    IReceiverChain resolve(Object chainId) throws ChainNotFoundException;

    /**
     * Get list of identifiers of all chains stored in this storage.
     *
     * @return list of identifiers of all chains
     */
    List<Object> enumerate();
}
