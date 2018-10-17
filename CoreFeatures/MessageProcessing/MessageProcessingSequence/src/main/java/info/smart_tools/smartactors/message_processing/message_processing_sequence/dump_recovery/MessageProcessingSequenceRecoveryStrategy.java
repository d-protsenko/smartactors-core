package info.smart_tools.smartactors.message_processing.message_processing_sequence.dump_recovery;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing.message_processing_sequence.MessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.NestedChainStackOverflowException;
import info.smart_tools.smartactors.class_management.module_manager.ModuleManager;

import java.util.Collection;
import java.util.Iterator;

/**
 * IOC strategy that recovers a {@link MessageProcessingSequence} from {@link IObject} created by call of {@link
 * MessageProcessingSequence#dump(IObject) dump()} method of original sequence.
 */
public class MessageProcessingSequenceRecoveryStrategy implements IResolveDependencyStrategy {
    private final IFieldName stepsStackFieldName;
    private final IFieldName chainsStackFieldName;
    private final IFieldName maxDepthFieldName;
    private final IFieldName chainsDumpFieldName;

    /**
     * The constructor.
     *
     * @throws ResolutionException if resolution of any dependencies failed
     */
    public MessageProcessingSequenceRecoveryStrategy()
            throws ResolutionException {
        stepsStackFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "stepsStack");
        chainsStackFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "chainsStack");
        maxDepthFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "maxDepth");
        chainsDumpFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "chainsDump");
    }

    @Override
    public <T> T resolve(final Object... args) throws ResolveDependencyStrategyException {
        try {
            IObject dump = (IObject) args[0];
            IObject message = (IObject) args[1];

            int maxDepth = ((Number) dump.getValue(maxDepthFieldName)).intValue();
            Iterator stepStack = ((Collection) dump.getValue(stepsStackFieldName)).iterator();
            Iterator chainsStack = ((Collection) dump.getValue(chainsStackFieldName)).iterator();
            IObject chainsDump = (IObject) dump.getValue(chainsDumpFieldName);

            IChainStorage storage = new ChainStorageDecorator(
                    IOC.resolve(Keys.getOrAdd(IChainStorage.class.getCanonicalName())),
                    chainsDump);

            // setcurrentmessage
            // ToDo: check if here we should not setup message for correct chain Id resolution
            // ToDo: but have to store resolved chain Ids in message processor
            // ToDo: also we can directly use chain from stack for sequence creation!!! then we've done it
            // ! Object mainChainId = IOC.resolve(Keys.getOrAdd("chain_id_from_map_name_and_message"), chainsStack.next(), message);
            int mainChainPos = ((Number) stepStack.next()).intValue();

            // ! IMessageProcessingSequence sequence = new MessageProcessingSequence(maxDepth, storage.resolve(mainChainId));
            IMessageProcessingSequence sequence = new MessageProcessingSequence(maxDepth, ((IReceiverChain)chainsStack.next()).getId(), null);
            sequence.goTo(0, mainChainPos + 1);

            int level = 1;

            while (stepStack.hasNext()) {
                // ToDo: check if here we should not setup message for correct chain Id resolution
                // ToDo: but have to store resolved chain Ids in message processor
                // ToDo: also we can directly use chain from stack for sequence creation!!! then we've done it
                // ! Object chainId = IOC.resolve(Keys.getOrAdd("chain_id_from_map_name_and_message"), chainsStack.next(), message);
                int pos = ((Number) stepStack.next()).intValue();

                // ! sequence.callChain(storage.resolve(chainId));
                sequence.callChain((IReceiverChain)chainsStack.next());
                sequence.goTo(level++, pos + 1);
            }

            sequence.next();

            return (T) sequence;
        } catch (ResolutionException | ReadValueException | InvalidArgumentException | ClassCastException
                // | ChainNotFoundException
                | NestedChainStackOverflowException e) {
            throw new ResolveDependencyStrategyException(e);
        }
    }
}
