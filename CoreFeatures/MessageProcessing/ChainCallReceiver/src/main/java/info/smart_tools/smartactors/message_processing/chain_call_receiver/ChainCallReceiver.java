package info.smart_tools.smartactors.message_processing.chain_call_receiver;

import info.smart_tools.smartactors.class_management.class_loader_management.VersionManager;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing.chain_call_receiver.exceptions.ChainChoiceException;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.MessageReceiveException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.NestedChainStackOverflowException;

/**
 * Receiver that calls {@link IReceiverChain} chosen by a {@link IChainChoiceStrategy} on a message.
 */
public class ChainCallReceiver implements IMessageReceiver {

    private final IFieldName externalAccessFieldName;
    private final IFieldName fromExternalFieldName;
    private final IFieldName accessForbiddenFieldName;

    private IChainStorage chainStorage;
    private IChainChoiceStrategy chainChoiceStrategy;

    /**
     * The constructor.
     *
     * @param chainStorage                  chains storage to use
     * @param chainChoiceStrategy           strategy to use
     * @throws InvalidArgumentException     if storage is {@code null}
     * @throws InvalidArgumentException     if strategy is {@code null}
     */
    public ChainCallReceiver(final IChainStorage chainStorage, final IChainChoiceStrategy chainChoiceStrategy)
            throws InvalidArgumentException, ResolutionException {
        externalAccessFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "externalAccess");
        fromExternalFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "fromExternal");
        accessForbiddenFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "accessToChainForbiddenError");
        if (null == chainStorage) {
            throw new InvalidArgumentException("Storage should not be null.");
        }

        if (null == chainChoiceStrategy) {
            throw new InvalidArgumentException("Strategy should not be null.");
        }

        this.chainStorage = chainStorage;
        this.chainChoiceStrategy = chainChoiceStrategy;
    }

    @Override
    public void receive(final IMessageProcessor processor)
            throws MessageReceiveException {
        try {
            Object chainId = chainChoiceStrategy.chooseChain(processor);
            VersionManager.setCurrentMessage(processor.getMessage());
            IReceiverChain chain = chainStorage.resolve(chainId);
            checkAccess(chain, processor);
            processor.getSequence().callChain(chain);
        } catch (ChainChoiceException | ChainNotFoundException | NestedChainStackOverflowException e) {
            throw new MessageReceiveException("Could not call nested chain.", e);
        }
    }

    private void checkAccess(final IReceiverChain chain, final IMessageProcessor processor)
            throws ChainChoiceException {
        try {
            boolean isExternal = (boolean) chain.getChainDescription().getValue(this.externalAccessFieldName);
            Boolean fromExternal = (Boolean) processor.getContext().getValue(fromExternalFieldName);
            if (null != fromExternal && fromExternal) {
                processor.getContext().setValue(fromExternalFieldName, false);
                if (!isExternal) {
                    processor.getContext().setValue(this.accessForbiddenFieldName, true);
                    //ToDo: need new constructor for all internal exceptions with string formatter
                    throw new ChainChoiceException("External access forbidden to chain - " + chain.getName() + ".");
                }
            }
        } catch (ReadValueException | ChangeValueException | InvalidArgumentException e) {
            throw new ChainChoiceException("Access forbidden.", e);
        }
    }

    @Override
    public void dispose() {
    }
}
