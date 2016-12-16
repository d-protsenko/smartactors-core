package info.smart_tools.smartactors.message_processing.message_processing_sequence.dump_recovery;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.exceptions.ChainCreationException;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.exceptions.ChainModificationException;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.message_processing_interfaces.irouter.IRouter;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ChainStorageDecorator implements IChainStorage {
    private final IChainStorage systemStorage;
    private final IObject dumps;
    private Map<Object, IReceiverChain> chainsCache = new HashMap<>();

    /**
     * The constructor.
     *
     * @param systemStorage    the system chain storage
     * @param dumps            {@link IObject} containing dumps of chains saved with sequence
     */
    public ChainStorageDecorator(final IChainStorage systemStorage, final IObject dumps) {
        this.systemStorage = systemStorage;
        this.dumps = dumps;
    }

    @Override
    public void register(final Object chainId, final IObject description) throws ChainCreationException {
        throw new ChainCreationException("Not supported", null);
    }

    @Override
    public void update(final Object chainId, final IObject modification) throws ChainNotFoundException, ChainModificationException {
        throw new ChainModificationException("Not supported", null);
    }

    @Override
    public IReceiverChain resolve(final Object chainId) throws ChainNotFoundException {
        if (chainsCache.containsKey(chainId)) {
            return chainsCache.get(chainId);
        }

        try {
            IFieldName dumpFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), chainId.toString());

            IObject dump = (IObject) dumps.getValue(dumpFieldName);

            IReceiverChain chain = null;

            if (dump == null) {
                chain = systemStorage.resolve(chainId);
            } else {
                IRouter router = IOC.resolve(Keys.getOrAdd(IRouter.class.getCanonicalName()));
                chain = IOC.resolve(Keys.getOrAdd(IReceiverChain.class.getCanonicalName()), chainId, dump, this, router);
            }

            chainsCache.put(chainId, chain);

            return chain;
        } catch (ResolutionException | ReadValueException | InvalidArgumentException e) {
            throw (ChainNotFoundException) new ChainNotFoundException(chainId).initCause(e);
        }
    }

    @Override
    public List<Object> enumerate() {
        // TODO: Implement?
        return Collections.emptyList();
    }
}
