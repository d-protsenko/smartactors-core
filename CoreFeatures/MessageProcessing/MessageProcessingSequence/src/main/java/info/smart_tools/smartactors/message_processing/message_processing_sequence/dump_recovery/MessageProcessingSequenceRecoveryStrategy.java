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
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.NestedChainStackOverflowException;

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
    private final IFieldName chainNameFieldName;

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
        chainNameFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "chain");
    }

    @Override
    public <T> T resolve(final Object... args) throws ResolveDependencyStrategyException {
        try {
            IObject dump = (IObject) args[0];
            IObject message = (IObject) args[1];

            int maxDepth = ((Number) dump.getValue(maxDepthFieldName)).intValue();
            Iterator stepStack = ((Collection) dump.getValue(stepsStackFieldName)).iterator();
            Iterator chainsStack = ((Collection) dump.getValue(chainsStackFieldName)).iterator();
            // ToDo: check if chain dump useless then remove it in MessageProcessingSequence
            // IObject chainsDump = (IObject) dump.getValue(chainsDumpFieldName);

            Object chainName = ((IReceiverChain)chainsStack.next()).getName();
            IMessageProcessingSequence sequence = new MessageProcessingSequence(maxDepth, chainName, message);

            int mainChainPos = ((Number) stepStack.next()).intValue();
            sequence.goTo(0, mainChainPos + 1);

            int level = 1;

            while (stepStack.hasNext()) {
                int pos = ((Number) stepStack.next()).intValue();
                chainName = ((IReceiverChain)chainsStack.next()).getName();
                sequence.callChain(chainName);
                sequence.goTo(level++, pos + 1);
            }

            sequence.next();

            return (T) sequence;
        } catch (ResolutionException | ReadValueException | InvalidArgumentException | ClassCastException
                | ChainNotFoundException | NestedChainStackOverflowException e) {
            throw new ResolveDependencyStrategyException(e);
        }
    }
}
